package swarm.twitter

import swarm.data.RefMap
import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import org.scalatra._
import swarm.Swarm

object SwarmBridge {

  def get(mapKey: String, key: String)(implicit tx: Transporter, local: Location) = {
    Swarm.spawnAndReturn {
      val stringsMap: RefMap[List[String]] = RefMap(classOf[List[String]], mapKey)
      stringsMap.get(key).getOrElse(Nil).map(x => <div style="margin: 10px; padding: 10px; border-bottom: 1px solid #999;">{x}</div>).toSeq
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
        <h1>SwarmTwitter</h1>
        <h2>Sample users:</h2>
        <div>
          <a href="/jmcdoe">jmcdoe</a>
        </div>
        <div>
          <a href="/maxpower">maxpower</a>
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
          <h1>SwarmTwitter</h1>
          <h2>Statuses:</h2>
          <div>
            {SwarmBridge.get(userId, "statuses")}
          </div>
            <hr/>
          <form action={"/" + userId} method="get">
            Status:
              <input type="text" size="15" name="status"/>
              <input type="submit" value="submit"/>
          </form>
          <a href="/">Home</a>
        </body>
      </html>
    }
  }
}