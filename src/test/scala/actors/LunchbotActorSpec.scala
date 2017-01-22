package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import application.{Application, TestApplication}
import com.typesafe.config.{Config, ConfigFactory}
import model.UserId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpecLike, MustMatchers}
import slack.api.BlockingSlackApiClient
import slack.models.Message
import slack.rtm.SlackRtmConnectionActor.SendMessage
import util.Formatting

class LunchbotActorSpec
  extends TestKit(ActorSystem("LunchbotActorSpec"))
    with FlatSpecLike
    with MustMatchers
    with ImplicitSender
    with ScalaFutures
    with MockitoSugar
    with Formatting {

  val testUser = "test_user"
  val selfId = "some_self_id"

  val config: Config = ConfigFactory.load()

  private def getMessage(text: String, userId: UserId = testUser): Message = {
    Message("", "", userId, text, None)
  }

  it should "only react when mentioned" in new TestApplication {

    val lunchbotActor = TestActorRef(LunchbotActor.props(selfId, messagesService, statisticsService, slackRtmClient.apiClient, config))

    val messageWithMention: Message = getMessage(s"<@$selfId> hey there!")

    lunchbotActor ! messageWithMention

    expectMsgClass(classOf[SendMessage])

    val messageWithoutMention: Message = getMessage("hey there!")

    lunchbotActor ! messageWithoutMention

    expectNoMsg()

  }

  it should "not react to own mentions" in new TestApplication {

    val lunchbotActor = TestActorRef(LunchbotActor.props(selfId, messagesService, statisticsService, slackRtmClient.apiClient, config))

    val messageWithMention: Message = getMessage(s"<@$selfId> hey there!", selfId)

    lunchbotActor ! messageWithMention

    expectNoMsg()

  }

}
