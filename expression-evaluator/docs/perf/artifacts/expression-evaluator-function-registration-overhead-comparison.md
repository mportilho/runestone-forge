| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| createCallSitesFromProviderClass | 144551.465 | 66.221 | -144485.244 | +99.95% |
| createCallSitesFromProviderInstance | 197274.384 | 143.266 | -197131.118 | +99.93% |
| defaultFunctionLookup | 11.538 | 9.455 | -2.083 | +18.05% |
| putFunctionsFromProviderPerRequest | 198226.670 | 338.870 | -197887.800 | +99.83% |

Media: before=135016.014 ns/op, after=139.453 ns/op, melhoria=+99.90%.
