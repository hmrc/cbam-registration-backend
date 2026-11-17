/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cbamregistrationbackend.models

import play.api.libs.json.Json
import uk.gov.hmrc.cbamregistrationbackend.base.SpecBase

class UserAnswersSpec extends SpecBase {

  val userAnswers: UserAnswers = emptyUserAnswers.copy(
    data = Json.obj("foo" -> "bar", "bool" -> true)
  )

  "UserAnswers" must {
    val json =
      s"""{"_id":"$testCredId","data":{"foo":"bar","bool":true},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}}}"""

    "serialise to json" in {
      Json.toJson(userAnswers).toString mustBe json
    }

    "deserialise from json" in {
      Json.parse(json).as[UserAnswers] mustBe userAnswers
    }
  }
}
