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

import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.cbamregistrationbackend.models.UserAnswers
import uk.gov.hmrc.cbamregistrationbackend.repositories.{UpdateFailure, UpdateSuccess, UserAnswersRepository}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserAnswersController @Inject() (
    userAnswersRepository: UserAnswersRepository,
    clock: Clock,
    override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  // TODO: Use actions to check user is authorised and has the right credId

  def get(credId: String): Action[AnyContent] =
    Action.async { implicit request =>
      userAnswersRepository.get(credId).map {
        case Some(userAnswers) => Ok(Json.toJson(userAnswers))
        case None              => NotFound
      }
    }

  def set(): Action[JsValue] =
    Action(parse.json).async { implicit request =>
      withJsonBody[UserAnswers] { userAnswers =>
        userAnswersRepository.set(userAnswers).map {
          case UpdateSuccess => Ok
          case UpdateFailure =>
            logger.warn(
              s"[UserAnswersController][set] No user answers record to be updated for credId ${userAnswers.credId}"
            )
            NotFound
        }
      }
    }

  def create(credId: String): Action[AnyContent] =
    Action.async { implicit request =>
      val newUserAnswers = UserAnswers(
        credId = credId,
        lastUpdated = Instant.now(clock)
      )

      userAnswersRepository.add(newUserAnswers).map { _ =>
        logger.info(s"[UserAnswersController][create] Created user answers for credId $credId")
        Created
      }
    }

  def delete(credId: String): Action[AnyContent] =
    Action.async { implicit request =>
      userAnswersRepository.clear(credId).map { _ =>
        logger.info(s"[UserAnswersController][delete] Cleared user answers for credId $credId")
        Ok
      }
    }

  def keepAlive(credId: String): Action[AnyContent] =
    Action.async { implicit request =>
      userAnswersRepository
        .keepAlive(credId)
        .map(_ => Ok)
    }
}
