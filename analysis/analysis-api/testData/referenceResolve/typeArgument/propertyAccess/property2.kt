// COMPILATION_ERRORS
val property: Int = 10

fun foo() {
    property<Int, <caret>String>
}
