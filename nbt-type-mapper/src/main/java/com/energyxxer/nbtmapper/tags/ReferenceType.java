package com.energyxxer.nbtmapper.tags;

import com.energyxxer.commodore.functionlogic.nbt.NBTTag;
import com.energyxxer.commodore.functionlogic.nbt.path.NBTPath;
import com.energyxxer.nbtmapper.NBTTypeMap;
import com.energyxxer.nbtmapper.PathContext;

public class ReferenceType extends DataType implements DeepDataType {

    private final String name;

    public ReferenceType(NBTTypeMap parent, String name) {
        this(parent, name, null);
    }

    public ReferenceType(NBTTypeMap parent, String name, TypeFlags flags) {
        super(parent);
        this.name = name;
        this.setFlags(flags);
    }

    @Override
    public void collectDataTypeFor(PathContext context, NBTPath path, DataTypeQueryResponse response) {
        if(this.flags != null && this.flags.hasFlag("protocol")) {
            PathProtocol protocol = PathProtocol.DEFAULT;
            if(name.equals("ENTITY")) protocol = PathProtocol.ENTITY;
            if(name.equals("BLOCK_ENTITY")) protocol = PathProtocol.BLOCK_ENTITY;
            this.parent.collectDataTypeForProtocol(protocol, null, context, path, response);
        } else {
            this.parent.collectDataTypeFor(name, context, path, response);
        }
    }

    public String getReferenceName() {
        return name;
    }

    @Override
    public Class<? extends NBTTag> getCorrespondingTagType() {
        return NBTTag.class;
    }

    @Override
    public String getShortTypeName() {
        return "Tag";
    }

    @Override
    public String toString() {
        return "$" + name + (flags != null ? " " + flags : "");
    }
}
