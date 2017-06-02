# PiManager
一个简易的通过读取按钮状态控制硬盘和服务,同时使用pwm控制散热的小程序.程序默认监听两个按钮,硬盘及服务按钮Pin0,风扇档位控制按钮Pin2
具体针脚编号请看http://pi4j.com/pins/model-b-plus.html

*必要的依赖库<br>
<a href="http://pi4j.com/install.html">PI4J</a><br>
<a href="http://wiringpi.com/download-and-install/">Wiring Pi</a> <br>
<a href="http://elinux.org/RPi_Java_JDK_Installation/">Java JDK</a> <br>
<a href="https://www.smartmontools.org/">smartmontools->(smartctl)</a> <br>
推荐使用在线安装的方式安装上述几个库,当前Pi4j稳定版为1.1. Wiring Pi稳定版为2.44. JDK1.8(推荐1.6以上)

<h5>请注意,树莓派最新版系统内核为4.9,此内核会将树莓派2B cpu识别为BCM2385 ,wiring库无法识别此cpu型号,将无法运行,请降级内核到4.4</h5>

*配置文件

```java
{
	"adjustInterval":1.5, //温度调整间隔,两次读取到的温度相差1.5度以上才会调整风扇转速
	"pauseTime":15000, //按下硬盘启动按钮后,风扇全速运转的时间
	"pauseWhenDiskStart":true, //是否在硬盘启动时暂停风扇,此项设置在树莓派和硬盘使用同一个电源适配器并且功率不大的情况下适用,如3.5寸硬盘 12V2A电源
  同时带树莓派,风扇,硬盘 最好还是打开,实测硬盘启动瞬间电流为1.8A(MAX),剩余留给树莓派的电流不多了,风扇一定要停掉, 有大功率电源可以设置为false
	"pinButtonDisk":0, //控制硬盘启停的按钮pin
	"pinButtonFan":2,  //控制风扇档位的按钮pin(通过按钮设置档位只是设置最大转速,如果 speed > set --> speed == set.当前一共五档)
	"pinLedDisk":3,    //指示磁盘启停状态的LED pin
	"pinLedFan":4,     //指示风扇档位的LED pin
	"pinPwmFan":1,     //风扇PWM针脚
	"pinRelayEN1":6,   //硬盘电源控制继电器针脚1
	"pinRelayEN2":5,   //硬盘电源控制继电器针脚2
	"sysMaxtemp":50,   //系统最大温度,如果当前超过此温度,风扇最大速度工作,档位控制失效
	"sysMintemp":30,   //系统最小温度,如果低于此温度,风扇停止工作
	"serviceList":[    //硬盘启动以后需要启动的服务.如果没有安装的服务请从配置文件中删除,否则启动后硬盘警告灯会一直闪烁表示启动过程有问题
		{
			"serviceStart":"service smbd start", //服务启动命令
			"serviceStatus":"service smbd status", //读取服务当前状态命令
			"serviceStatusCheck":"Active: active (running)", //服务状态检查命令
			"serviceStop":"service smbd stop"  //停止命令
		},
		{
			"serviceStart":"service transmission-daemon start",
			"serviceStatus":"service transmission-daemon status",
			"serviceStatusCheck":"Active: active (running)",
			"serviceStop":"service transmission-daemon stop"
		},
		{
			"serviceStart":"deluged",
			"serviceStatus":"ps -ef | grep deluged",
			"serviceStatusCheck":"/usr/bin/deluge",
			"serviceStop":"pkill deluged"
		}
	],
	"diskInfoList":[ //磁盘信息.通过配置磁盘UUID来确认找到的磁盘是不是当前要挂载的以及挂载点,上面的服务可能会用到如下载工具等
		{              //仅挂载配置的磁盘,三个UUID可以只设置一个.使用sudo blkid命令读取磁盘的UUID
			"PARTUUID":"",
			"PTUUID":"",
			"UUID":"76e7fb44-1a8d-473b-a6fe-21b4a41ef20d",
			"mountPoint":"/media/pi/wd/"
		}
	]
}
```
