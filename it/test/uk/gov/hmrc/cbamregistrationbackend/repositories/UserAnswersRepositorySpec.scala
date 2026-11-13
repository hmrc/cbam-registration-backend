/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.cbamregistrationbackend.repositories

import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.cbamregistrationbackend.config.AppConfig
import uk.gov.hmrc.cbamregistrationbackend.models.UserAnswers
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext

class UserAnswersRepositorySpec
    extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.dbTimeToLiveInSeconds) thenReturn 100L

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  protected override val repository: UserAnswersRepository = new UserAnswersRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".add" must {
    "save the supplied user answers" in {
      val addResult   = repository.add(userAnswers).futureValue
      val savedRecord = find(Filters.equal("_id", userAnswers.credId)).futureValue.headOption.value

      addResult   mustEqual true
      savedRecord mustEqual userAnswers
    }

    "not fail (upsert) if called twice" in {
      val newUserAnswers = userAnswers.copy(lastUpdated = instant)

      val addResult1  = repository.add(userAnswers).futureValue
      val addResult2  = repository.add(newUserAnswers).futureValue
      val savedRecord = find(Filters.equal("_id", userAnswers.credId)).futureValue.headOption.value

      addResult1  mustEqual true
      addResult2  mustEqual true
      savedRecord mustEqual newUserAnswers
    }
  }

  ".set" must {
    "set the last updated time on the supplied user answers to `now`, and save them" in {
      insert(userAnswers).futureValue

      val newUserAnswers = userAnswers.copy(lastUpdated = instant)
      val setResult      = repository.set(newUserAnswers).futureValue
      val updatedRecord  = find(Filters.equal("_id", userAnswers.credId)).futureValue.headOption.value

      setResult     mustEqual UpdateSuccess
      updatedRecord mustEqual newUserAnswers
    }

    "fail to update if the user answers were not previously saved" in {
      val newUserAnswers = userAnswers.copy(lastUpdated = instant)
      val setResult      = repository.set(newUserAnswers).futureValue

      setResult mustEqual UpdateFailure
    }
  }

  ".get" when {
    "there is a record for this id" must {
      "update the lastUpdated time and get the record" in {
        insert(userAnswers).futureValue

        val result         = repository.get(userAnswers.credId).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "there is no record for this id" must {
      "return None" in {
        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" must {
    "remove a record" in {
      insert(userAnswers).futureValue

      val result = repository.clear(userAnswers.credId).futureValue

      result mustEqual true

      repository.get(userAnswers.credId).futureValue must not be defined
    }

    "return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" when {
    "there is a record for this id" must {
      "update its lastUpdated to `now` and return true" in {
        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.credId).futureValue

        val expectedUpdatedAnswers = userAnswers.copy(lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("_id", userAnswers.credId)).futureValue.headOption.value

        result         mustEqual true
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "there is no record for this id" must {
      "return true" in {
        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }
}
