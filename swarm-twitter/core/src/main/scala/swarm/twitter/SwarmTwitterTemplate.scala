package swarm.twitter

import swarm.data.RefMap
import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import org.scalatra._
import java.util.{UUID, Date}
import swarm.{NoBee, Swarm}

class SwarmTwitterTemplate(nodeName: String, localPort: Short, remotePort: Short) extends ScalatraServlet with UrlSupport {

  implicit val local: Location = new InetLocation(java.net.InetAddress.getLocalHost, localPort)
  val remote: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, remotePort)
  implicit val tx: Transporter = InetTransporter

  RefMap.locations = List(local, remote)
  InetTransporter.listen(localPort)

  type Status = Tuple3[String, String, Date]

  get("/") {
    <html>
      <head>
        <title>SwarmTwitter :: {nodeName}</title>
      </head>
      <body>
        <div style="width: 800px; margin-left: auto; margin-right: auto;">
          <h1>SwarmTwitter :: {nodeName}</h1>
          <p>Welcome to SwarmTwitter, a Twitter simulator built using <a href="https://github.com/sanity/Swarm">Swarm</a>, a framework allowing the creation of web applications which can scale transparently through a novel portable continuation-based approach.</p>
          <p>To use SwarmTwitter, follow the links below to act as any of the sample users, or add your own users by browsing to <span style="font-face: monospace;">/&lt;username&gt;</span>.</p>
          <div><a href={"http://localhost:8080/"}>view node 1</a></div>
          <div><a href={"http://localhost:8081/"}>view node 2</a></div>
          <h2>Sample users</h2>
          <div>
            <a href="/jmcdoe">jmcdoe</a>
          </div>
          <div>
            <a href="/maxpower">maxpower</a>
          </div>
          {statuses(List("jmcdoe", "maxpower"))}
        </div>
      </body>
    </html>
  }

  get("/:userId") {
    val userId: String = params("userId")
    if (params.contains("status")) {
      Swarm.spawn {
        val status = params("status")
        val stringsMap = RefMap(classOf[List[Status]], "statuses")
        val statuses: List[Status] = stringsMap.get(userId).getOrElse(Nil)
        stringsMap.put(local, userId, new Status(userId, status, new Date) :: statuses)
      }
      redirect("/" + userId)
    } else {
      <html>
        <head>
          <title>SwarmTwitter :: {nodeName}</title>
        </head>
        <body>
          <div style="width: 800px; margin-left: auto; margin-right: auto;">
            <h1>SwarmTwitter :: {nodeName}</h1>
            <h2>
              {userId}
            </h2>
            {statuses(List(userId))}
            <hr/>
            <form action={"/" + userId} method="get">
              Post a new status:
              <input type="text" size="15" name="status"/>
              <input type="submit" value="submit"/>
            </form>
            <div><a href={"http://localhost:8080/" + userId}>view node 1</a></div>
            <div><a href={"http://localhost:8081/" + userId}>view node 2</a></div>
            <div><a href="/">go home</a></div>
          </div>
        </body>
      </html>
    }
  }

  def statuses(userIds: List[String]): xml.NodeSeq = {
    val uuid = UUID.randomUUID.toString

    // TODO it's unnecessarily expensive to move data from node to node simply to accumulate a resulting data set.  come up with some kind of client accumulator layer which collects data to be finally returned only once, wihtout passing it from node to node when unnecessary
    Swarm.spawn {
      val stringsMap = RefMap(classOf[List[Status]], "statuses")
      var statusesList: List[Status] = Nil

      // TODO create an implicit conversion so we can support for comprehensions within continuations, then use to iterate over userIds list
      Swarm.foreach(userIds, {
        userId: String =>
          val statuses = stringsMap.get(userId)
          if (statuses != None) statusesList = statuses.get ::: statusesList
      })

      statusesList = statusesList.sortWith((s1, s2) => (s1._3 compareTo s2._3) >= 0)
      Swarm.saveFutureResult(uuid, statusesList)
    }

    val statuses = Swarm.getFutureResult(uuid).asInstanceOf[List[Status]] // TODO boo casting

    <h3>Statuses</h3>
    <div style="margin-bottom: 50px;">
      {
        statuses.map(status =>
          <div style="margin: 10px; padding: 10px; border-bottom: 1px solid #999;">
            <span style="color: #9999ff">{status._1}&gt;</span> {status._2} <span style="float: right; color: #999999; font-size: 75%">{status._3}</span>
          </div>
        ).toSeq
      }
    </div>
  }

  protected def contextPath = request.getContextPath
}