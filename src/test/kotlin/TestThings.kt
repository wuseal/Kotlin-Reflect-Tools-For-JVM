/**
 * Created by Seal.Wu on 2017/10/27.
 */


val topName = "topSeal"
private val topAge = 666
private val topAgeName = "666"
private fun preTopAge(): Int {
    return funPropertyReduceAge(topAge)
}

private fun nextTopAge(): Int {
    return funPropertyPlusAge(topAge)
}


val funPropertyPlusAge: (Int) -> Int = { age -> age + 1 }

val funPropertyReduceAge: (Int) -> Int = { age -> age - 1 }

fun funDoubleAge(age: Int): Int {
    return age * 2
}

class TestDemo {
    private val name = "seal"
    val age = 28

    private fun isMan(): Boolean {
        return true
    }

    fun nextAge(): Int {
        return age + 1
    }
}