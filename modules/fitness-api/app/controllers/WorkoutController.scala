package api.controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import api.model.{Workout, WorkoutStep}
import api.dao.WorkoutDao
import api.security.{Authenticator, Secured, SimpleAuthenticator}
import com.evojam.play.elastic4s.PlayElasticFactory
import com.evojam.play.elastic4s.configuration.ClusterSetup
import org.joda.time.DateTime
import api.model.Responses

import scala.util.{Failure, Success}


class WorkoutController @Inject() (workoutResource: WorkoutDao,  auth: SimpleAuthenticator) extends Controller with Secured {
  override val authService: Authenticator = auth
  def getHardcoded = Action {
    val workout = Workout(
      workoutId = "workout-1",
      name = "Workout 1",
      imageURL = Some("https://cdn.muscleandstrength.com/sites/all/themes/mnsnew/images/taxonomy/workouts/strength.jpg"),
      steps = List(
        WorkoutStep("Deadlifts", 30, 5),
        WorkoutStep("Squats", 25, 5),
        WorkoutStep("Pushup", 30, 5),
        WorkoutStep("Squat Jumps", 25, 5),
        WorkoutStep("Tricep Dips", 30, 5)),
      totalTime = 30
    )
    Ok(Json.toJson(workout))
  }

  def populate() = Action.async {
    workoutResource.bulkIndex(cannedBulkInput) map (_ => Ok) recoverWith {
      case err => Future.successful(InternalServerError(err.getMessage))
    }
  }

  def search(q: String) = Action.async {
    workoutResource.searchByQueryString(q) map {
      case workouts if workouts.length > 0 =>
        Ok(Json.toJson(workouts)).withHeaders("X-Total-Count" -> workouts.length.toString)
      case empty => NoContent
    }
  }

  def get(id: String) = Action.async {
    workoutResource.getById(id) map {
      case Some(workout) => Ok(Json.toJson(workout))
      case None => InternalServerError("Could not find workout")
    }
  }
  
  def createWorkout() = Action.async { request =>
    request.body.asJson.map { json =>
      json.asOpt[Workout] match {
        case Some(workout) => workoutResource.indexObject(workout) map (_ => Ok) recoverWith {
          case err => Future.successful(InternalServerError(err.getMessage))
        }
        case None => Future.successful(BadRequest("Could not parse Json"))
      }
    } getOrElse { Future.successful(BadRequest("Could not get request body as JSON")) }
  }

  val cannedBulkInput = List(
   Workout(
      workoutId = "workout-1",
      name = "Workout 1",
      imageURL = Some("https://cdn.muscleandstrength.com/sites/all/themes/mnsnew/images/taxonomy/workouts/strength.jpg"),
      steps = List(
        WorkoutStep("Deadlifts", 30, 5),
        WorkoutStep("Squats", 25, 5),
        WorkoutStep("Pushup", 30, 5),
        WorkoutStep("Squat Jumps", 25, 5),
        WorkoutStep("Tricep Dips", 30, 5)),
      totalTime = 30
    ),
   Workout(
      workoutId = "workout-2",
      name = "Workout 2",
      imageURL = Some("https://cdn.muscleandstrength.com/sites/all/themes/mnsnew/images/taxonomy/workouts/strength.jpg"),
      steps = List(
        WorkoutStep("Curls", 10, 5),
        WorkoutStep("Skull Crushers", 20, 5),
        WorkoutStep("Squats", 10, 5),
        WorkoutStep("Deadlifts", 30, 5),
        WorkoutStep("Pullups", 15, 5)),
      totalTime = 40
    ),
    Workout(
      workoutId = "workout-3",
      name = "Killer Abs",
      imageURL = None,
      steps = List(
          WorkoutStep("V-Ups", 50, 5),
          WorkoutStep("Side-to-side V-Ups", 50, 5),
          WorkoutStep("Straddle Ups", 50, 5),
          WorkoutStep("1-leg V-Ups", 50, 5),
          WorkoutStep("Punching Crunchups", 50, 5),
          WorkoutStep("Crunches", 50, 5)),
       totalTime = 12
    ))
}
