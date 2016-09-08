# Generic derivation for circe

This project contains four implementations of generic derivation for [circe](https://github.com/travisbrown/circe):

* `raw`: A macro implementation (based on Argonaut's derivation support) that doesn't depend on Shapeless and provides a limited range of features.
* `simple`: A straightforward Shapeless-based implementation that doesn't work for some corner cases.
* `dryer`: An even more elegant implementation that uses Shapeless's `LabelledTypeClass`.
* `better`: An improved version of `simple` with more complete support and more reasonable prioritization.

`better` is close to equivalent to circe-generic before the 0.4.0 release.

These are provided primarily for pedagogical purposes, but can also be useful for comparing the
complexity or performance of the different approaches.

## License

These examples are licensed under the **[Apache License, Version 2.0][apache]**
(the "License"); you may not use this software except in compliance with the
License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[apache]: http://www.apache.org/licenses/LICENSE-2.0
