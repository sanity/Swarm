package swarm.twitter

import swarm.data.RefMap
import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import org.scalatra._
import swarm.Swarm

object SwarmBridge {

  def get(mapKey: String, key: String)(implicit tx: Transporter, local: Location) = {
    Swarm.spawnAndReturn {
      val stringsMap: RefMap[List[String]] = RefMap(classOf[List[String]], mapKey)
      stringsMap.get(key).getOrElse(Nil).map(x => <div style="margin: 10px; padding: 10px; border-bottom: 1px solid #999;">
        {x}
      </div>).toSeq
    }
  }

  def update(mapKey: String, key: String, value: String)(implicit tx: Transporter, local: Location) {
    Swarm.spawn {
      val stringsMap = RefMap(classOf[List[String]], mapKey)
      val statuses: List[String] = stringsMap.get(key).getOrElse(Nil)
      stringsMap.put(local, key, value :: statuses)
    }
  }
}

class SwarmTwitterTemplate(localPort: Short, remotePort: Short) extends ScalatraServlet with UrlSupport {

  implicit val local: Location = new InetLocation(java.net.InetAddress.getLocalHost, localPort)
  val remote: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, remotePort)

  implicit val tx: Transporter = InetTransporter

  println("local: " + localPort)
  println("remote: " + remotePort)
  RefMap.locations = List(local, remote)
  InetTransporter.listen(localPort)

  protected def contextPath = request.getContextPath

  get("/") {
    <html>
      <head>
        <title>SwarmTwitter</title>
      </head>
      <body>
        <div style="width: 800px; margin-left: auto; margin-right: auto;">
          <h1>SwarmTwitter</h1>
          <p>Welcome to SwarmTwitter, a Twitter simulator built using <a href="https://github.com/sanity/Swarm">Swarm</a>, a framework allowing the creation of web applications which can scale transparently through a novel portable continuation-based approach.</p>
          <p>To use SwarmTwitter, follow the links below to act as any of the sample users, or add your own users by browsing to <span style="font-face: monospace;">/&lt;username&gt;</span>.</p>
          <h2>Sample users</h2>
          <div>
            <a href="/jmcdoe">jmcdoe</a>
          </div>
          <div>
            <a href="/maxpower">maxpower</a>
          </div>
        </div>
      </body>
    </html>
  }

  get("/:userId") {
    val userId: String = params("userId")
    if (params.contains("status")) {
      val status = params("status")
      SwarmBridge.update(userId, "statuses", status)
      redirect("/" + userId)
    } else {
      <html>
        <head>
          <title>SwarmTwitter</title>
        </head>
        <body>
          <div style="width: 800px; margin-left: auto; margin-right: auto;">
            <h1>SwarmTwitter</h1>
            <h2>
              {userId}
            </h2>
            <h3>Statuses</h3>
            <div style="margin-bottom: 50px;">
              {SwarmBridge.get(userId, "statuses")}
            </div>
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
}