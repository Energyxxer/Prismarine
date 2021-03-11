package com.energyxxer.enxlex.pattern_matching.structures;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenListMatch;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.List;

public class TokenList extends TokenPattern<TokenPattern<?>[]> {
	private final TokenPattern<?>[] patterns;
	private TokenPattern<?>[] contentExcludingSeparators = null;

	public TokenList(TokenPatternMatch source, TokenPattern<?>[] patterns) {
		super(source);
		this.patterns = patterns;
	}

	public int size() {
	    return patterns.length;
    }
	
	@Override
	public TokenList setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public TokenPattern<?>[] getContents() {
		return patterns;
	}

	public TokenPattern<?>[] getContentsExcludingSeparators() {
		if(contentExcludingSeparators == null) {
			TokenPatternMatch separatorSource = ((TokenListMatch) this.source).getSeparatorMatch();
			if(separatorSource == null) return getContents();

			ArrayList<TokenPattern<?>> filtered = new ArrayList<>();

			for(TokenPattern<?> entry : patterns) {
				if(entry.source != null && entry.source != separatorSource) {
					filtered.add(entry);
				}
			}
			contentExcludingSeparators = filtered.toArray(new TokenPattern<?>[0]);
		}
		return contentExcludingSeparators;
	}

	@Override
	public String toString() {
		StringBuilder o = new StringBuilder("(L)" + ((name != null && name.length() > 0) ? (name + ": ") : "") + "[ ");
		
		for(TokenPattern<?> p : patterns) {
			o.append(p.toString());
		}
		o.append(" ]");
		return o.toString();
	}

	@Override
	public List<Token> search(TokenType type) {
		ArrayList<Token> list = new ArrayList<>();
		for(TokenPattern<?> p : patterns) {
			if(p.getContents() instanceof Token) {
				if(((Token) p.getContents()).type == type) list.add((Token) p.getContents());
			}
		}
		return list;
	}

	@Override
	public List<Token> deepSearch(TokenType type) {
		ArrayList<Token> list = new ArrayList<>();
		for(TokenPattern<?> p : patterns) {
			list.addAll(p.deepSearch(type));
		}
		return list;
	}

	@Override
	public List<TokenPattern<?>> searchByName(String name) {
		ArrayList<TokenPattern<?>> list = new ArrayList<>();
		for(TokenPattern<?> p : patterns) {
			if(p.name.equals(name)) list.add(p);
		}
		return list;
	}

	@Override
	public List<TokenPattern<?>> deepSearchByName(String name) {
		ArrayList<TokenPattern<?>> list = new ArrayList<>();
		for(TokenPattern<?> p : patterns) {
			if(p.name.equals(name)) list.add(p);
			list.addAll(p.deepSearchByName(name));
		}
		return list;
	}

	@Override
	public TokenPattern<?> find(String path) {
		if(isPathInCache(path)) return getCachedFindResult(path);
		String[] subPath = path.split("\\.",2);

		List<TokenPattern<?>> next = searchByName(subPath[0]);
		if(next.size() <= 0) return null;
		if(subPath.length == 1) return next.get(0);
		return putFindResult(path, next.get(0).find(subPath[1]));
	}

	@Override
	public String flatten(boolean separate) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < patterns.length; i++) {
			String str = patterns[i].flatten(separate);
			sb.append(str);
			if(!str.isEmpty() && i < patterns.length-1 && separate) sb.append(" ");
		}
		return sb.toString();
	}

	@Override
	public TokenSource getSource() {
		if(patterns == null) return null;
		for(TokenPattern pattern : patterns) {
			TokenSource source = pattern.getSource();
			if(source != null) return source;
		}
		return null;
	}

	@Override
	public StringLocation getStringLocation() {
		if (patterns == null || patterns.length <= 0) return null;
		StringLocation l = null;
		for (TokenPattern<?> pattern : patterns) {
			StringLocation loc = pattern.getStringLocation();
			if(l == null) {
				l = loc;
				continue;
			}
			if(loc.index < l.index) {
				l = loc;
			}
		}
		return l;
	}

	@Override
	public StringBounds getStringBounds() {
		if (patterns == null || patterns.length <= 0) return null;
		StringLocation start = null;
		StringLocation end = null;

		//Find start
		for (TokenPattern<?> pattern : patterns) {
			StringLocation loc = pattern.getStringLocation();
			if(start == null) {
				start = loc;
				continue;
			}
			if(loc.index < start.index) {
				start = loc;
			}
		}

		//Find end
		for (TokenPattern<?> pattern : patterns) {
			StringLocation loc = pattern.getStringBounds().end;
			if(end == null) {
				end = loc;
				continue;
			}
			if(loc.index > end.index) {
				end = loc;
			}
		}
		return new StringBounds(start, end);
	}

    @Override
    public ArrayList<Token> flattenTokens(ArrayList<Token> list) {
        for(TokenPattern<?> pattern : patterns) {
			pattern.flattenTokens(list);
        }
        return list;
    }

	@Override
	public String getType() {
		return "LIST";
	}

	@Override
	public TokenList addTags(List<String> newTags) {
		super.addTags(newTags);
		return this;
	}

	@Override
	public void validate() {
		this.validated = true;
		if(this.name != null && this.name.length() > 0) this.tags.add(name);
		for(TokenPattern<?> p : patterns) {
			for(String tag : this.tags) {
				if(!tag.startsWith("__")) p.addTag(tag);
			}
			p.validate();
		}
	}
}
