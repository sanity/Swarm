package swarm.demos

import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import org.scalatra._
import java.util.{UUID, Date}
import swarm.Swarm
import swarm.collection.RefMap

object SwarmTwitter extends App {
  import org.mortbay.jetty._
  import org.mortbay.jetty.servlet._

  val jettyPorts = List(8080 to 8083).flatten
  for (jettyPort <- jettyPorts) launchNode(jettyPort, (jettyPort + 1000))

  def launchNode(jettyPort: Int, localSwarmPort: Int) = {
    val server  = new Server(jettyPort)
    val context = new Context(server, "/", Context.SESSIONS)
    val node    = new SwarmTwitter(localSwarmPort)
    context.addServlet(new ServletHolder(node), "/*")
    server.start();
  }
}

// This is necessary because of Scalatra's use of DynamicVariable instances (to hold thread local HTTP request and response objects) which don't serialize during Swarm moves
object SwarmBridge {

  type Status = Tuple3[String, String, Date]

  def statuses(userIds: List[String])(implicit tx: Transporter, local: Location): List[Status] = {
    val uuid = Swarm.remember {

      val statusesMap = RefMap(classOf[List[Status]], "statuses")
      var statusesList: List[Status] = Nil

      // TODO create an implicit conversion so we can support for comprehensions within continuations, then use to iterate over userIds list
      Swarm.foreach(userIds, {
        userId: String =>
          val statuses = statusesMap.get(userId)
          if (statuses != None) statusesList = statuses.get ::: statusesList
      })

      statusesList.sortWith((s1, s2) => (s1._3 compareTo s2._3) >= 0)
    }
    Swarm.getFutureResult(uuid).asInstanceOf[List[Status]] // TODO boo casting
  }

  def addStatus(userId: String, status: String)(implicit tx: Transporter, local: Location) = Swarm.spawn {
    val statusesMap = RefMap(classOf[List[Status]], "statuses")
    val statuses: List[Status] = statusesMap.get(userId).getOrElse(Nil)
    statusesMap.put(local, userId, new Status(userId, status, new Date) :: statuses)
  }

  def addFollowee(userId: String, followee: String)(implicit tx: Transporter, local: Location) = Swarm.spawn {
    val followeesMap = RefMap(classOf[List[String]], "followees")
    val followees: List[String] = followeesMap.get(userId).getOrElse(Nil)
    followeesMap.put(local, userId, followee :: followees)
  }

  def followeeIds(userId: String)(implicit tx: Transporter, local: Location): List[String] = {
    val uuid = Swarm.remember {
      RefMap(classOf[List[String]], "followees").get(userId).getOrElse(Nil)
    }
    Swarm.getFutureResult(uuid).asInstanceOf[List[String]]
  }
}

// TODO consider using comet to avoid annoyingly necessary refreshing
class SwarmTwitter(localPort: Int) extends ScalatraServlet {

  implicit val local: Location = new InetLocation(java.net.InetAddress.getLocalHost, localPort)
  implicit val tx: Transporter = InetTransporter

  RefMap.add(local)

  println("listening")
  InetTransporter.listen(localPort)
  println("after listening")

  type Status = (String, String, Date)

  get("/") {
    <html>
      <head>
        <title>
          { "SwarmTwitter on " + local.toString }
        </title>
      </head>
      <body>
        <div style="width: 800px; margin-left: auto; margin-right: auto;">
          <h1>
            { "SwarmTwitter on " + local.toString }
          </h1>
          <p>Welcome to SwarmTwitter, a Twitter simulator built using <a href="https://github.com/sanity/Swarm">Swarm</a>, a framework allowing the creation of web applications which can scale transparently through a novel portable continuation-based approach.</p>
          <p>To use SwarmTwitter, follow the links below to act as any of the sample users, or add your own users by browsing to <span style="font-face: monospace;">/&lt;username&gt;</span>.</p>
          {
            SwarmTwitter.jettyPorts.map(i =>
              <div>
                <a href={ "http://localhost:" + i }>{ "http://localhost:" + i }</a>
              </div>).toSeq
          }
          <h2>Sample users</h2>
          <div>
            <a href="/jmcdoe">jmcdoe</a>
          </div>
          <div>
            <a href="/maxpower">maxpower</a>
          </div>{ statuses(List("jmcdoe", "maxpower")) }
        </div>
      </body>
    </html>
  }

  get("/:userId") {
    val userId: String = params("userId")
    if (params.contains("status")) {
      val status = params("status")
      SwarmBridge.addStatus(userId, status)
      redirect("/" + userId)
    } else if (params.contains("followee")) {
      val followee = params("followee")
      SwarmBridge.addFollowee(userId, followee)
      redirect("/" + userId)
    } else {
      <html>
        <head>
          <title>
            { "SwarmTwitter on " + local.toString }
          </title>
        </head>
        <body>
          <div style="width: 800px; margin-left: auto; margin-right: auto;">
            <h1>
              { "SwarmTwitter on " + local.toString }
            </h1>
            <h2>
              { userId }
            </h2>{ statuses(userAndFollowees(userId)) }<hr/>
            <form action={ "/" + userId } method="get">
              Follow a user:
              <input type="text" size="15" name="followee"/>
              <input type="submit" value="follow"/>
            </form>
            <form action={ "/" + userId } method="get">
              Post a new status:
              <input type="text" size="15" name="status"/>
              <input type="submit" value="post"/>
            </form>
            {
              SwarmTwitter.jettyPorts.map(i =>
                <div>
                  <a href={ "http://localhost:" + i + "/" + userId }>{ "http://localhost:" + i + "/" + userId }</a>
                </div>).toSeq
            }
            <div>
              <a href="/">go home</a>
            </div>
          </div>
        </body>
      </html>
    }
  }

  def userAndFollowees(userId: String): List[String] = {
    userId :: SwarmBridge.followeeIds(userId)
  }

  def statuses(userIds: List[String]): xml.NodeSeq = {
    val statuses = SwarmBridge.statuses(userIds)
    <h3>Statuses</h3>
    <div style="margin-bottom: 50px;">
      {
        statuses.map(status =>
          <div style="margin: 10px; padding: 10px; border-bottom: 1px solid #999;">
            <span style="color: #9999ff">
              { status._1 }
              &gt;
            </span>{ status._2 }<span style="float: right; color: #999999; font-size: 75%">
                                  { status._3 }
                                </span>
          </div>).toSeq
      }
    </div>
  }
}
