import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.MatchesRegex

package object model {
  type Iso2CodeRefine = NonEmpty And MatchesRegex["[A-Z]{2}"]
  type Iso2CodeString = String Refined Iso2CodeRefine
}
