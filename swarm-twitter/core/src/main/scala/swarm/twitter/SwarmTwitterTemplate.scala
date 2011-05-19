package swarm.twitter

import swarm.data.RefMap
import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import swarm.Swarm
import org.scalatra._

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
    if (params.contains("x")) {
      val x = params("x")
      Swarm.spawn {
        val stringsMap = RefMap(classOf[String], "strings")
        stringsMap.put(local, "x", x)
      }
      redirect("/")
    } else {
      <html>
        <head>
          <title>SwarmTwitter</title>
        </head>
        <body>
          <h1>SwarmTwitter</h1>
          <div>X:
            {Swarm.spawnAndReturn {
            val stringsMap: RefMap[String] = RefMap(classOf[String], "strings")
            stringsMap.get("x")
          }}
          </div>
          <div>
            <a href="/">refresh</a>
          </div>
          <form action="/" method="get">
            X:
              <input type="text" size="5" name="x"/>
              <input type="submit" value="apply"/>
          </form>
        </body>
      </html>
    }
  }
}