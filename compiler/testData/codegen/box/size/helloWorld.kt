// TARGET_BACKEND: WASM

// RUN_THIRD_PARTY_OPTIMIZER
// WASM_DCE_EXPECTED_OUTPUT_SIZE: wasm  41_564
// WASM_DCE_EXPECTED_OUTPUT_SIZE: mjs    6_342
// WASM_OPT_EXPECTED_OUTPUT_SIZE:        9_384

fun box(): String {
    println("Hello, World!")
    return "OK"
}
