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

package uk.gov.hmrc.cbamregistrationbackend.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, status}
import uk.gov.hmrc.cbamregistrationbackend.base.SpecBase
import uk.gov.hmrc.cbamregistrationbackend.repositories.{UpdateFailure, UpdateSuccess, UserAnswersRepository}

import scala.concurrent.Future

class UserAnswersControllerSpec extends SpecBase {

  val mockUserAnswersRepository: UserAnswersRepository = mock[UserAnswersRepository]

  val controller = new UserAnswersController(
    mockUserAnswersRepository,
    clock,
    cc
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersRepository)
  }

  ".get" must {
    "return 200 OK with an existing user answers when there is one for the id" in {
      when(mockUserAnswersRepository.get(eqTo(testCredId))).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val result: Future[Result] = controller.get(testCredId)(fakeRequest)

      status(result)        mustBe OK
      contentAsJson(result) mustBe Json.toJson(emptyUserAnswers)

      verify(mockUserAnswersRepository, times(1)).get(eqTo(testCredId))
    }

    "return 404 NOT_FOUND when there are no user answers for the id" in {
      when(mockUserAnswersRepository.get(eqTo(testCredId))).thenReturn(Future.successful(None))

      val result: Future[Result] = controller.get(testCredId)(fakeRequest)

      status(result) mustBe NOT_FOUND

      verify(mockUserAnswersRepository, times(1)).get(eqTo(testCredId))
    }
  }

  ".set" must {
    "return 200 OK when user answers are updated" in {
      when(mockUserAnswersRepository.set(any())).thenReturn(Future.successful(UpdateSuccess))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result) mustBe OK

      verify(mockUserAnswersRepository, times(1)).set(eqTo(emptyUserAnswers))
    }

    "return 404 NOT_FOUND if no record could be updated" in {
      when(mockUserAnswersRepository.set(any())).thenReturn(Future.successful(UpdateFailure))

      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson(emptyUserAnswers))
        )

      status(result) mustBe NOT_FOUND

      verify(mockUserAnswersRepository, times(1)).set(eqTo(emptyUserAnswers))
    }

    "return 400 BAD_REQUEST if the request body cannot be parsed as UserAnswers" in {
      val result: Future[Result] =
        controller.set()(
          fakeRequestWithJsonBody(Json.toJson("invalid"))
        )

      status(result) mustBe BAD_REQUEST

      verify(mockUserAnswersRepository, times(0)).set(any())
    }
  }

  ".create" must {
    "create a fresh user answers for the user and return 201 CREATED" in {
      when(mockUserAnswersRepository.add(any())).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.create(testCredId)(fakeRequest)

      status(result) mustBe CREATED

      verify(mockUserAnswersRepository, times(1)).add(eqTo(emptyUserAnswers))
    }
  }

  ".delete" must {
    "clear user answers and return 200 OK" in {
      when(mockUserAnswersRepository.clear(eqTo(testCredId))).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.delete(testCredId)(fakeRequest)

      status(result) mustBe OK

      verify(mockUserAnswersRepository, times(1)).clear(eqTo(testCredId))
    }
  }

  ".keepAlive" must {
    "call the repository method keepAlive and return 200 OK" in {
      when(mockUserAnswersRepository.keepAlive(eqTo(testCredId))).thenReturn(Future.successful(true))

      val result: Future[Result] = controller.keepAlive(testCredId)(fakeRequest)

      status(result) mustBe OK

      verify(mockUserAnswersRepository, times(1)).keepAlive(eqTo(testCredId))
    }
  }
}
