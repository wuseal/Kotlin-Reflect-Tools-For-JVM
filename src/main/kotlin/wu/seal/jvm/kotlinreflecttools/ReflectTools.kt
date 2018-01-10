package wu.seal.jvm.kotlinreflecttools

import java.lang.IllegalStateException
import java.lang.reflect.Modifier
import java.util.*
import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.PropertyReference
import kotlin.reflect.KProperty

/**
 * Kotlin reflect tools for JVM
 * Created by Seal.Wu on 2017/10/27.
 */

/**
 * change the property value with new value int the special KProperty in package level property ,not the property in class
 *
 */
fun <R> changeTopPropertyValue(property: KProperty<R>, newValue: R): Boolean =
        changePropertyValue(null, property, newValue)

/**
 * change the property value with new value int the special KProperty inside a  class level ,not the property not in any class
 */
fun <R> changeClassPropertyValue(classObj: Any, property: KProperty<R>, newValue: R): Boolean =
        changePropertyValue(classObj, property, newValue)

private fun <R> changePropertyValue(classObj: Any?, property: KProperty<R>, newValue: R): Boolean {
    val owner = (property as PropertyReference).owner
    val propertyName = property.name
    val containerClass: Class<*>
    try {
        containerClass = (owner!!::class.members as ArrayList).firstOrNull { it.name == "jClass" }?.call(owner) as Class<*>
    } catch (e: Exception) {
        throw IllegalArgumentException("No such property 'jClass'")
    }
    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true
            val modifyFiled = field.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(field, modifyFiled.getInt(field) and Modifier.FINAL.inv())

            field.set(classObj, newValue)
            return true
        }
    }
    return false
}

/**
 * change the property value with new value int the special property name inside a class level ,not the property not in any class
 */
fun <R> changeClassPropertyValueByName(classObj: Any, propertyName: String, newValue: R): Boolean {
    val containerClass: Class<*> = classObj::class.java

    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true
            val modifyFiled = field.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(field, modifyFiled.getInt(field) and Modifier.FINAL.inv())

            field.set(classObj, newValue)
            return true
        }
    }
    return false
}

/**
 * change the property value with new value int the special Property name in package level property ,not the property in class
 */
fun changeTopPropertyValueByName(otherCallableReference: CallableReference, propertyName: String, newValue: Any?) {

    val owner = otherCallableReference.owner
    val containerClass: Class<*>
    try {
        containerClass = (owner!!::class.members as ArrayList).firstOrNull { it.name == "jClass" }?.call(owner) as Class<*>
    } catch (e: Exception) {
        throw IllegalArgumentException("No such property 'jClass'")
    }

    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true
            val modifyFiled = field.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(field, modifyFiled.getInt(field) and Modifier.FINAL.inv())
            modifyFiled.setInt(field, modifyFiled.getInt(field) and Modifier.PRIVATE.inv())
            /**
             * top property(package property) should be static in java level
             * or throw an exception
             * */
            val clazz = when (field.type) {
                Int::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticIntegerFieldAccessorImpl")
                Long::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticLongFieldAccessorImpl")
                Double::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticDoubleFieldAccessorImpl")
                Float::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticFloatFieldAccessorImpl")
                Boolean::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticBooleanFieldAccessorImpl")
                else -> Class.forName("sun.reflect.UnsafeQualifiedStaticObjectFieldAccessorImpl")
            }
            val constructor = clazz.declaredConstructors[0]
            constructor.isAccessible = true

            val customAccess = constructor.newInstance(field, false)

            field.javaClass.declaredMethods.forEach { method ->
                if (method.name == "setFieldAccessor") {
                    method.isAccessible = true

                    method.invoke(field, customAccess, true)
                }
            }
            if (Modifier.isStatic(field.modifiers)) {
                field.set(null, newValue)
            } else {
                throw IllegalStateException("It is not a top property : $propertyName")
            }
            return
        }
    }
    throw IllegalArgumentException("Can't find the property named :$propertyName in the same file with ${otherCallableReference.name}")
}

/**
 * change the property value with new value int the special property name inside a class level ,not the property not in any class
 * it likes `changeClassPropertyValueByName` but it can't change the property value to any other type value ,not only with the original property value type.
 */
fun <R> changeClassPropertyValueByNameIgnoreType(classObj: Any, propertyName: String, newValue: R): Boolean {
    val containerClass: Class<*> = classObj::class.java

    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true
            val modifyFiled = field.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(field, modifyFiled.getInt(field) and Modifier.FINAL.inv())

            changeClassPropertyValueByName(field, "type", (newValue as Any)::class.java)


            val root = getClassPropertyValueByName(field, "root")
            root?.let {
                changeClassPropertyValueByName(root, "type", (newValue as Any)::class.java)
            }
            val clazz =
                    if (Modifier.isStatic(field.modifiers)) {
                        when ((newValue as Any)::class.java) {
                            Int::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticIntegerFieldAccessorImpl")
                            Int::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedStaticIntegerFieldAccessorImpl")
                            Long::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticLongFieldAccessorImpl")
                            Int::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedStaticLongFieldAccessorImpl")
                            Double::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticDoubleFieldAccessorImpl")
                            Double::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedStaticDoubleFieldAccessorImpl")
                            Float::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticFloatFieldAccessorImpl")
                            Float::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedStaticFloatFieldAccessorImpl")
                            Boolean::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedStaticBooleanFieldAccessorImpl")
                            Boolean::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedStaticBooleanFieldAccessorImpl")
                            else -> Class.forName("sun.reflect.UnsafeQualifiedStaticObjectFieldAccessorImpl")
                        }
                    } else {
                        when ((newValue as Any)::class.java) {
                            Int::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedIntegerFieldAccessorImpl")
                            Int::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedIntegerFieldAccessorImpl")
                            Long::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedLongFieldAccessorImpl")
                            Int::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedLongFieldAccessorImpl")
                            Double::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedDoubleFieldAccessorImpl")
                            Double::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedDoubleFieldAccessorImpl")
                            Float::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedFloatFieldAccessorImpl")
                            Float::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedFloatFieldAccessorImpl")
                            Boolean::class.javaPrimitiveType -> Class.forName("sun.reflect.UnsafeQualifiedBooleanFieldAccessorImpl")
                            Boolean::class.javaObjectType -> Class.forName("sun.reflect.UnsafeQualifiedBooleanFieldAccessorImpl")
                            else -> Class.forName("sun.reflect.UnsafeQualifiedObjectFieldAccessorImpl")
                        }

                    }
            val constructor = clazz.declaredConstructors[0]
            constructor.isAccessible = true

            val customAccess = constructor.newInstance(field, false)
            field.javaClass.declaredMethods.forEach { method ->
                if (method.name == "setFieldAccessor") {
                    method.isAccessible = true

                    method.invoke(field, customAccess, true)
                }
            }
            field.set(classObj, newValue)


            return true
        }
    }
    return false
}

/**
 * get the property value from a class object ,no matter whether the property is public ,private or intenel
 */
fun getClassPropertyValueByName(classObj: Any, propertyName: String): Any? {
    val containerClass: Class<*> = classObj::class.java

    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true

            return field.get(classObj)
        }
    }
    return null
}

/**
 * get the property value in the top of a kotlin file(not in any kotlin class) ,no matter whether the property is public ,private or internal
 */
fun getTopPropertyValueByName(otherCallableReference: CallableReference, propertyName: String): Any? {

    val owner = otherCallableReference.owner
    val containerClass: Class<*>
    try {
        containerClass = (owner!!::class.members as ArrayList).firstOrNull { it.name == "jClass" }?.call(owner) as Class<*>
    } catch (e: Exception) {
        throw IllegalArgumentException("No such property 'jClass'")
    }

    containerClass.declaredFields.forEach { field ->
        if (field.name == propertyName) {
            field.isAccessible = true

            /**
             * top property(package property) should be static in java level
             * or throw an exception
             * */
            if (Modifier.isStatic(field.modifiers)) {
                return field.get(null)
            } else {
                throw IllegalStateException("It is not a top property : $propertyName")
            }
        }
    }
    throw IllegalArgumentException("Can't find the property named :$propertyName in the same file with ${otherCallableReference.name}")
}

/**
 * invoke a method by name from a classObj,no matter whether the property is public ,private or internal
 */
fun invokeClassMethodByMethodName(classObj: Any, methodName: String, vararg methodArgs: Any?): Any? {
    val containerClass: Class<*> = classObj::class.java

    containerClass.declaredMethods.forEach { method ->
        if (method.name == methodName) {
            method.isAccessible = true
            val modifyFiled = method.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(method, modifyFiled.getInt(method) and Modifier.FINAL.inv())

            if (methodArgs.isNotEmpty()) {

                return method.invoke(classObj, *methodArgs)
            } else {
                return method.invoke(classObj)
            }
        }
    }
    throw IllegalArgumentException("Can't find the method named :$methodName in the classObj : $classObj")
}

/**
 * invoke a method by name from a kotlin file(not in any kotlin class),no matter whether the property is public ,private or internal
 */
fun invokeTopMethodByMethodName(otherCallableReference: CallableReference, methodName: String, vararg methodArgs: Any?): Any? {
    val owner = otherCallableReference.owner
    val containerClass: Class<*>
    try {
        containerClass = (owner!!::class.members as ArrayList).firstOrNull { it.name == "jClass" }?.call(owner) as Class<*>
    } catch (e: Exception) {
        throw IllegalArgumentException("No such property 'jClass'")
    }
    containerClass.declaredMethods.forEach { method ->
        if (method.name == methodName) {
            method.isAccessible = true
            val modifyFiled = method.javaClass.getDeclaredField("modifiers")
            modifyFiled.isAccessible = true
            modifyFiled.setInt(method, modifyFiled.getInt(method) and Modifier.FINAL.inv())

            if (methodArgs.isNotEmpty()) {
                return method.invoke(null, *methodArgs)
            } else {
                return method.invoke(null)
            }
        }
    }
    throw IllegalArgumentException("Can't find the method named :$methodName in the same file with ${otherCallableReference.name}")

}