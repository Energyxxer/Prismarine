package com.energyxxer.enxlex.pattern_matching.structures;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.util.StringBounds;
import com.energyxxer.util.StringLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class TokenGroup extends TokenPattern<TokenPattern<?>[]> {
	private final TokenPattern<?>[] patterns;

	public TokenGroup(TokenPatternMatch source, TokenPattern<?>[] patterns) {
		super(source);
		this.patterns = patterns;
	}

	@Override
	public TokenPattern<?>[] getContents() {
		return patterns;
	}
	
	@Override
	public TokenGroup setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String toString() {
		String o = ((name != null && name.length() > 0) ? name + ": " : "") + "{ ";
		
		for(TokenPattern<?> p : patterns) {
			o += p.toString();
		}
		o += " }";
		return o;
	}

	@Override
	public void collect(TokenType type, List<Token> list) {
		for(TokenPattern<?> p : patterns) {
			if(p.getContents() instanceof Token) {
				if(((Token) p.getContents()).type == type) list.add((Token) p.getContents());
			}
		}
	}

	@Override
	public void deepCollect(TokenType type, List<Token> list) {
		for(TokenPattern<?> p : patterns) {
			p.deepCollect(type, list);
		}
	}

	@Override
	public TokenPattern<?> getByName(String name) {
		for(TokenPattern<?> p : patterns) {
			if(p.name.equals(name)) return p;
		}
		return null;
	}

	@Override
	public void collectByName(String name, List<TokenPattern<?>> list) {
		for(TokenPattern<?> p : patterns) {
			if(p.name.equals(name)) list.add(p);
		}
	}

	@Override
	public void deepCollectByName(String name, List<TokenPattern<?>> list) {
		for(TokenPattern<?> p : patterns) {
			if(p.name.equals(name)) list.add(p);
			p.deepCollectByName(name, list);
		}
	}

	@Override
	public TokenPattern<?> find(String path) {
		if(isPathInCache(path)) return getCachedFindResult(path);
		int dotIndex = path.indexOf('.');
		String name;
		String nextPath;
		if(dotIndex < 0) {
			name = path;
			nextPath = null;
		} else {
			name = path.substring(0, dotIndex);
			nextPath = path.substring(dotIndex+1);
		}

		TokenPattern<?> next = getByName(name);
		TokenPattern<?> result;
		if(next == null || nextPath == null) {
			result = next;
		} else {
			result = next.find(nextPath);
		}
		return putFindResult(path, result);
	}

	@Override
	public String flatten(String delimiter) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < patterns.length; i++) {
			String str = patterns[i].flatten(delimiter);
			sb.append(str);
			if(!str.isEmpty() && i < patterns.length-1 && !delimiter.isEmpty()) sb.append(delimiter);
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
			if(loc != null && loc.index < l.index) {
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
			if(loc != null) {
				if (start == null) {
					start = loc;
					continue;
				}
				if (loc.index < start.index) {
					start = loc;
				}
			}
		}
		if(start == null) return null;

		//Find end
		for (TokenPattern<?> pattern : patterns) {
			StringBounds bounds = pattern.getStringBounds();
			if(bounds != null) {
				if (end == null) {
					end = bounds.end;
					continue;
				}
				if (bounds.end.index > end.index) {
					end = bounds.end;
				}
			}
		}

		if(end == null) return null;
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
		return "GROUP";
	}

	@Override
	public TokenGroup addTags(List<String> newTags) {
		super.addTags(newTags);
		return this;
	}

	@Override
	public void validate() {
		this.validated = true;
		for(TokenPattern<?> p : patterns) {
			p.validate();
		}
	}

    @Override
    public void traverse(Consumer<TokenPattern<?>> consumer, Stack<TokenPattern<?>> stack) {
		if(stack != null) stack.push(this);
		consumer.accept(this);
		for(TokenPattern<?> p : patterns) {
			p.traverse(consumer, stack);
		}
		if(stack != null) stack.pop();
    }

    @Override
    public int endIndex() {
        return patterns[patterns.length-1].endIndex();
    }
}
