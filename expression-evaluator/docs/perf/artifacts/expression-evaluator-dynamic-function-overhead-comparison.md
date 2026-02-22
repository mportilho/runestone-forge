| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| dynamicConstantFunction | 161.385 | 103.196 | -58.188 | +36.06% |
| dynamicVariableFunction | 267.355 | 213.380 | -53.975 | +20.19% |
| dynamicFunctionInsideSequence | 1560.836 | 905.657 | -655.179 | +41.98% |
| nativeConstantMath | 31.382 | 31.183 | -0.199 | +0.63% |
| nativeVariableMath | 175.149 | 175.215 | +0.066 | -0.04% |

Foco dinamico medio: before=663.192 ns/op, after=407.411 ns/op, melhoria=+38.57%.
