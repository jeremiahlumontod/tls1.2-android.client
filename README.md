# tls1.2-android.client

a sample android client for TLS1.2 implementation
NOTE: the code includes a hack that prevents checking the certificate holder, the
fix would be to create a certificate for the certificate user, like for example,
i used my machine for deploying the web application in tomcat which is by
standard can be access via localhost, but in the case of android, it will not
work, it will only work in 10.0.2.2.

i also included 10.0.2.3/4/5 to my DNS for this test to work

finally, activate the one way or two way authentication in your server (i used tomcat)
and use the corresponding java code for that. there are two classes that needs
adjustments for this: MainActivity.java button.setOnClickListener for ip:port
port 80 for standard access and port 8443 for ssl/tls access

in the same file, MyTask class doInBackground(), select the implemetation of http access,
at the moment there are two, HttpsManagerLocalhost for one way authentication
and HttpsManagerApacheLib for two way authentication. i just comment / uncomment
the line i needed to test


by the way, the TLS1.2 script for generating certificates did not include how to generate
keystore and truststore that is compatible with android, please use the following lines
in addition to that script to generate the BKS format for android. the BouncyCastle
library is in the root directory. you can download it yourself if you want.

/**create bks truststore for android
keytool -importkeystore -srckeystore tomcatclienttruststore.jks -srcstoretype JKS -srcstorepass 123456 -destkeystore tomcatclienttruststore.bks -deststoretype BKS -deststorepass 123456 -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-156.jar
/**create bks keystore for android
keytool -importkeystore -srckeystore tomcatclient.jks -srcstoretype JKS -srcstorepass 123456 -destkeystore tomcatclient.bks -deststoretype BKS -deststorepass 123456 -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-156.jar
/**view contents
keytool -list -v -keystore tomcatclienttruststore.bks -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-156.jar -storetype BKS -storepass 123456
keytool -list -v -keystore tomcatclient.bks -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-156.jar -storetype BKS -storepass 123456


i used Android Studio by the way for this project