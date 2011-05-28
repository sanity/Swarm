package swarm.twitter

import swarm.data.RefMap
import swarm.transport.{Location, InetLocation, Transporter, InetTransporter}
import org.scalatra._
import swarm.Swarm

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
    val stringsMap = Swarm.spawnAndReturn(RefMap(classOf[List[String]], "statuses"))
    if (params.contains("status")) {
      val status = params("status")
      val statuses: List[String] = stringsMap.get(userId).getOrElse(Nil)
      stringsMap.put(local, userId, status :: statuses)
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
              {
              stringsMap.get(userId).getOrElse(Nil).map(x => <div style="margin: 10px; padding: 10px; border-bottom: 1px solid #999;">
                {x}
              </div>).toSeq
              }
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