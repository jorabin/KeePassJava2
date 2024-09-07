Properties that have sensitive values are called "Protected Properties". Up to 
version KeePassJava2 2.2, what is, and what is not, a protected property is determined
by the database that is loaded from file, which sets protection on a property
by property basis. 

The database also specifies that new Entries should have fields of  
specified names should be protected by default, typically this is restricted to
the `password` property. Up to version 2.2 new databases followed this convention 
by setting `password` as the only property protected by default.

In the KeePass format, protected properties are encrypted in addition to the 
encryption applied to the database as a whole when saved as a KeePass file.  

Up to version 2.2 of KeePassJava2 protected property values have been held as 
unencrypted Strings in memory and have been accessed as Strings using the 
`String getProperty(String)` and `String getPassword(String)` methods.

From version 2.3 of KeePassJava2, the storage of property values is controlled by the
`PropertyValue` interface. Users of KeePassJava2 may set implementations
of `PropertyValue` to be used for unprotected and protected properties
so that they may have control over how those property values are stored in memory.

The method 

`void Database.setPropertyValueStrategy(PropertyValue.Builder unprotectedPropertyBuilder,
                                                      PropertyValue.Builder protectedPropertyBuilder);`
                                                      
is used to set the strategy. By default `PropertyValue.Default` and `PropertyValue.Protected`
are set as the means of storage of values.

New accessors for property values are provided in the `Entry` class.

`PropertyValue getPropertyValue(String)` and `void setPropertyValue(String, PropertyValue)`
are intended as the primary means of access to property values. 

`String getPropertyValue(String)` and `void setPropertyValue(String, String)`
are now deprecated. `getPropertyValue` continues to retrieve both protected
and unprotected values as String. `setPropertyValue` stores values as either
protected or unprotected according to whether `Database.shouldProtect` returns
true or false.

A new method `Database.setProtectByDefault` is added to control whether
properties of a particular name will be protected or not. Calling this method
after a database is loaded causes the protection of all fields of all entries
affected to be updated to use the strategy defined on the Database.





