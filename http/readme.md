#Keepasshttp module

**_Experimental and Unsafe_**

This is a Java implementation of the [keepasshttp plugin](https://github.com/pfn/keepasshttp/)
for Windows KeePass. 

It is intended to provide support for the [passifox and chromeipass](https://github.com/pfn/passifox)
browser extensions that allow pasting of credentials from a KeePassDatabse 
in a browser context.

This implementation is an initial one and interworking may not be as good as it could be.

Also you should note that it does not use https so it is not secure.

Finally, you should note that any client can connect to the server since the protocol
does not support client credentials. So if you expose this server to the world,
then anyone with a compatible browser plugin can in theory connect
to your database and extract all your secrets.

**You have been warned sufficently that no representation is made as to
fitness for purpose, safety or anything else.**