# HOW TO ENABLE SSL IN THE ISSUE GATEWAY APIs

- By default, SSL is not enabled in the issue gateway APIs.
- By enabling the SSL REST APIs can be secured and post enablement all the APIs will be accessible using https protocol only.
- Below is a step by step guide to help with the SSL enablement but any other approach , which is common for any spring application , can also be used.   

## Steps to enable SSL 

- Get an SSL certificate. In this document generation of self-signed certificate is explained but you can also get a certificate from an authority .
	1. Open the command prompt
	2. Following is an example of command that can be used to create a set of cryptographic keys and store it in a keystore using keytool (a utility bundled with JRE):
		```sh
        keytool -genkeypair -alias issuegateway -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore issuegateway.p12 -validity 3650
        ```
	3. The above command will be an interative execution so please provide the values as appropriate.Please note the password provided and location of keystore generated.
	3. The above will generate the keystore in PKCS12 format.
- Download the issue gateway source code from the repository: https://github.com/hclproducts/appscan-issue-gateway.git .
- Copy the issuegateway.p12 file under /src/main/resources/keystore folder.
- Create application.properties file
	1. Create application.properties file (if not already present ) under /src/main/resources/ folder.
	2. Add the following parameter to the application.properties file. 
		
		server.ssl.key-store-type=PKCS12

		server.ssl.key-store=classpath:keystore/issuegateway.p12

        server.ssl.key-store-password=password

        server.ssl.key-alias=issuegateway
		
- Build issue gateway using the following command: mvn package
- The resultant build can be found under the appscan-issue-gateway\target folder.