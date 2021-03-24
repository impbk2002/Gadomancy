package makeo.gadomancy.common.utils;

import cpw.mods.fml.relauncher.Side;
import makeo.gadomancy.api.GadomancyApi;
import makeo.gadomancy.api.golems.AdditionalGolemType;
import makeo.gadomancy.client.events.ResourceReloadListener;
import makeo.gadomancy.common.Gadomancy;
import makeo.gadomancy.common.entities.golems.types.RemovedGolemType;
import net.minecraftforge.common.util.EnumHelper;
import thaumcraft.common.entities.golems.EnumGolemType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Gadomancy Mod
 * Gadomancy is Open Source and distributed under the
 * GNU LESSER GENERAL PUBLIC LICENSE
 * for more read the LICENSE file
 *
 * Created by makeo @ 29.07.2015 01:07
 */
public class GolemEnumHelper {
    private static final RemovedGolemType REMOVED_GOLEM_TYPE = new RemovedGolemType();
    private static final Injector INJECTOR = new Injector(EnumGolemType.class);
    private static final Injector ENUM_INJECTOR = new Injector(Enum.class);
    private static final Injector HELPER_INJECTOR = new Injector(EnumHelper.class);

    private static Field valuesField;
    private static Field getValuesField() {
        if(GolemEnumHelper.valuesField == null) {
            for(Field field : EnumGolemType.class.getDeclaredFields()) {
                String name = field.getName();
                if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) //Added 'ENUM$VALUES' because Eclipse's internal compiler doesn't follow standards
                {
                    GolemEnumHelper.valuesField = field;
                    GolemEnumHelper.valuesField.setAccessible(true);
                    break;
                }
            }
        }
        return GolemEnumHelper.valuesField;
    }

    private static final Class[] ENUM_PARAMS = {int.class, int.class, float.class, boolean.class, int.class,
            int.class, int.class, int.class};

    private static Field ordinalField;
    public static Field getOrdinalField() {
        if(GolemEnumHelper.ordinalField == null) {
            try {
                GolemEnumHelper.ordinalField = Enum.class.getDeclaredField("ordinal");
            } catch (NoSuchFieldException ignored) { }
        }
        return GolemEnumHelper.ordinalField;
    }

    private static final Class[] MAKE_ENUM_PARAMS = { Class.class, String.class, int.class, Class[].class, Object[].class };

    private static EnumGolemType createEnum(String name, int ordinal, AdditionalGolemType type) {
        return GolemEnumHelper.HELPER_INJECTOR.invokeMethod("makeEnum", GolemEnumHelper.MAKE_ENUM_PARAMS,
                EnumGolemType.class, name, ordinal, GolemEnumHelper.ENUM_PARAMS, new Object[]{ type.maxHealth, type.armor, type.movementSpeed,
                        type.fireResist, type.upgradeAmount, type.carryLimit, type.regenDelay, type.strength });
    }

    private static void addEnum(int ordinal, EnumGolemType type) {
        EnumGolemType[] values = GolemEnumHelper.resizeValues(ordinal + 1);
        values[ordinal] = type;
    }

    private static EnumGolemType addEnum(String name, int ordinal, AdditionalGolemType type) {
        EnumGolemType enumEntry = GolemEnumHelper.createEnum(name, ordinal, type);
        GolemEnumHelper.addEnum(ordinal, enumEntry);
        type.setEnumEntry(enumEntry);
        return enumEntry;
    }

    private static EnumGolemType[] resizeValues(int size) {
        EnumGolemType[] values = GolemEnumHelper.INJECTOR.getField(GolemEnumHelper.getValuesField());
        if(size > values.length) {
            EnumGolemType[] newValues = new EnumGolemType[size];
            System.arraycopy(values, 0, newValues, 0, values.length);

            for(int i = values.length; i < newValues.length; i++) {
                newValues[i] = GolemEnumHelper.createEnum("REMOVED", i, GolemEnumHelper.REMOVED_GOLEM_TYPE);
            }
            GolemEnumHelper.setValues(newValues);
            return newValues;
        }
        return values;
    }

    private static void setValues(EnumGolemType[] values) {
        try {
            EnumHelper.setFailsafeFieldValue(GolemEnumHelper.getValuesField(), null, values);
        } catch (Exception ignored) { }
    }

    public static EnumGolemType addGolemType(String name, AdditionalGolemType type) {
        return GolemEnumHelper.addEnum(name, GolemEnumHelper.getOrdinal(name), type);
    }

    private static int getOrdinal(String name) {
        Map<String, Integer> map = GolemEnumHelper.getCurrentMapping();

        if(map.containsKey(name)) {
            return map.get(name);
        }

        int returnVal = -1;
        int i = GolemEnumHelper.calcDefaultGolemCount();
        do {
            boolean contains = false;
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                if(entry.getValue() == i) {
                    contains = true;
                }
            }

            if(!contains) {
                returnVal = i;
            }
            i++;
        } while (returnVal < 0);

        map.put(name, returnVal);
        GolemEnumHelper.saveCurrentMapping(map);

        return returnVal;
    }

    public static void reorderEnum() {
        GolemEnumHelper.reorderEnum(GolemEnumHelper.getCurrentMapping());
    }

    public static void reorderEnum(Map<String, Integer> mapping) {
        EnumGolemType[] oldValues = EnumGolemType.values();
        GolemEnumHelper.resetEnum();
        for(Map.Entry<String, Integer> entry : mapping.entrySet()) {
            for(EnumGolemType type : oldValues) {
                if(type.name().equals(entry.getKey())) {
                    GolemEnumHelper.ENUM_INJECTOR.setObject(type);
                    GolemEnumHelper.ENUM_INJECTOR.setField(GolemEnumHelper.getOrdinalField(), entry.getValue());
                    GolemEnumHelper.addEnum(entry.getValue(), type);
                }
            }
        }

        new Injector(EnumGolemType.class).setField("codeToTypeMapping", null);
        if(Gadomancy.proxy.getSide() == Side.CLIENT) {
            ResourceReloadListener.getInstance().reloadGolemResources();
        }
    }

    private static void resetEnum() {
        EnumGolemType[] newValues = Arrays.copyOfRange(EnumGolemType.values(), 0, GolemEnumHelper.calcDefaultGolemCount());
        GolemEnumHelper.setValues(newValues);
    }

    private static int calcDefaultGolemCount() {
        EnumGolemType[] values = EnumGolemType.values();
        for(int i = 0; i < values.length; i++) {
            if(GadomancyApi.isAdditionalGolemType(values[i])) {
                return i;
            }
        }
        return values.length;
    }

    public static void validateSavedMapping() {
        if(GolemEnumHelper.hasCurrentMapping()) {
            Map<String, Integer> mapping = GolemEnumHelper.getCurrentMapping();
            for(Map.Entry<String, Integer> entry : GolemEnumHelper.defaultMapping.entrySet()) {
                if(!mapping.containsKey(entry.getKey())) {
                    mapping.put(entry.getKey(), GolemEnumHelper.getOrdinal(entry.getKey()));
                }
            }
        }
    }

    private static Map<String, Integer> defaultMapping = new HashMap<String, Integer>();

    public static Map<String, Integer> getCurrentMapping() {
        if(GolemEnumHelper.hasCurrentMapping()) {
            return Gadomancy.getModData().get("GolemTypeMapping", new HashMap<String, Integer>());
        } else if(Gadomancy.getModData() != null) {
            Gadomancy.getModData().set("GolemTypeMapping", GolemEnumHelper.defaultMapping);
        }
        return GolemEnumHelper.defaultMapping;
    }

    public static boolean hasCurrentMapping() {
        return Gadomancy.getModData() != null && Gadomancy.getModData().contains("GolemTypeMapping");
    }

    private static void saveCurrentMapping(Map<String, Integer> map) {
        if(Gadomancy.getModData() != null) {
            Gadomancy.getModData().set("GolemTypeMapping", map);
        }
    }
}
