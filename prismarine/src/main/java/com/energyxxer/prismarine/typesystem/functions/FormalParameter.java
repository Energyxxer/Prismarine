package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.ValueConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FormalParameter {
    @NotNull
    private String name;
    @NotNull
    private TypeConstraints constraints;
    @Nullable
    private ValueConstraints valueConstraints;

    public FormalParameter(@NotNull String name, @NotNull TypeConstraints constraints) {
        this(name, constraints, null);
    }

    public FormalParameter(@NotNull String name, @NotNull TypeConstraints constraints, @Nullable ValueConstraints valueConstraints) {
        this.name = name;
        this.constraints = constraints;
        this.valueConstraints = valueConstraints;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }


    @NotNull
    @Deprecated
    public TypeConstraints getConstraints() {
        return constraints;
    }
    @Deprecated
    public void setConstraints(@NotNull TypeConstraints constraints) {
        this.constraints = constraints;
    }


    @NotNull
    public TypeConstraints getTypeConstraints() {
        return constraints;
    }
    public void setTypeConstraints(@NotNull TypeConstraints constraints) {
        this.constraints = constraints;
    }

    @Nullable
    public ValueConstraints getValueConstraints() {
        return valueConstraints;
    }
    public void setValueConstraints(@Nullable ValueConstraints valueConstraints) {
        this.valueConstraints = valueConstraints;
    }

    @Override
    public String toString() {
        return name + " : " + constraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormalParameter that = (FormalParameter) o;
        return Objects.equals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraints);
    }

    public static boolean parameterListEquals(List<FormalParameter> a, List<FormalParameter> b) {
        if(a.size() != b.size()) return false;
        for(int i = 0; i < a.size(); i++) {
            if(!TypeConstraints.constraintsEqual(a.get(i).constraints, b.get(i).constraints)) {
                return false;
            }
        }
        return true;
    }

}
