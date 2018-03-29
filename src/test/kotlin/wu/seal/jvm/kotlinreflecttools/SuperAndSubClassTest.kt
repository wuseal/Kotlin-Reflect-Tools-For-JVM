package wu.seal.jvm.kotlinreflecttools

import com.winterbe.expekt.should
import org.junit.Test


/**
Created By: Seal.Wu
Date: 2018/3/29
Time: 10:43
 */
class SuperAndSubClassTest {

    @Test
    fun getSuperClassFieldValueTest() {
        val obj = SubClass()
        obj.getPropertyValue("superFieldOne").should.be.equal("superFieldOne")
    }

    @Test
    fun invokeSuperClassMethodTest() {
        val obj = SubClass()
        obj.invokeMethod("superPrivateFun" ,true).should.be.equal(true)
    }

    @Test
    fun invokeCommonNameFunInSubClass() {
        val obj = SubClass()
        obj.invokeMethod("getSignString").should.be.equal("Sub")
    }

    @Test
    fun changeSuperClassFieldValue() {
        val obj = SubClass()
        val newValue = "newValue"
        obj.changePropertyValue("superFieldOne",newValue)
        obj.getPropertyValue("superFieldOne").should.be.equal(newValue)
    }
}