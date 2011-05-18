package swarm.twitter

import javax.servlet.http.{HttpServlet, HttpServletRequest => HSReq, HttpServletResponse => HSResp}
import swarm.Swarm
import swarm.data.RefMap
import swarm.transport._

class SwarmTwitter extends HttpServlet {

  val localPort: Short = 9997
  val remotePort: Short = 9998

  implicit val local: Location = new InetLocation(java.net.InetAddress.getLocalHost, localPort)
  val remote: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, remotePort)

  implicit val tx: Transporter = InetTransporter

  RefMap.locations = List(local, remote)
  InetTransporter.listen(localPort)

  override def doGet(req: HSReq, resp: HSResp) = {

    val x = req.getParameter("x")
    if (x != null) {
      Swarm.spawn {
        val stringsMap = RefMap(classOf[String], "strings")
        stringsMap.put(local, "x", x)
      }
      resp.sendRedirect("/")
    }
    resp.getWriter().print(<html>
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
    )
  }
}