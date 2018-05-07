dubbo学习

注意：
运行DemoProvider或者DemoConsumer时，可能会报
java.lang.IllegalStateException: Can't assign requested address
	
Caused by: java.net.SocketException: Can't assign requested address

main ERROR container.Main:  [DUBBO] Can't assign requested address, dubbo version: 2.0.0, current host: 127.0.0.1

java.lang.IllegalStateException: Can't assign requested address
Caused by: java.net.SocketException: Can't assign requested address
等错误，这要在VM option中加-Djava.net.preferIPv4Stack=true再运行
