
fun main(args: Array<String>) {
    if (args.size > 1) {
        println("too many arguments given. call only with argument HOSTNAME as *.host.name")
    }
    val host = if (args.isEmpty()) {
        println("please specify hostname as *.host.name")
        readln()
    } else {
        args[0]
    }
    println("running for host $host")

    val le = LetsEncrypt(host)
    le.requestCert()
}