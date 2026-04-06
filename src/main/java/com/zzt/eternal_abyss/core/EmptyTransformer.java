package com.zzt.eternal_abyss.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class EmptyTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
