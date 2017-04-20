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


