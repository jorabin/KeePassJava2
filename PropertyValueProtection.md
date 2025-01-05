# Property Values

## Protected Properties

KeePass allows distinguishing more sensitive string valued properties from less sensitive. Properties
that have sensitive values are called "Protected Properties". 

KDBX files contain a list of the standard properties and whether they are to be treated as protected
by default. The standard properties include Password, which is so treated. There doesn't seem to be a way of updating 
this list in the Windows KeePass implementation, and in any case documentation says that the
list is updated to default after load of a database, so it seems that it is ignored.

Individual properties can be marked as "protect in memory", but there is no way to indicate that 
all properties of that name should be protected.

## KeePass (Windows) Implementation of Protected Properties

What KeePass means by property protection is firstly that they are additionally encrypted inside the 
overall encryption of the database file, this is so that when they are loaded as unencrypted XML
they are not exposed within the process memory. Instead, they are decrypted and immediately stored in a
secure way (DPAPI in Windows) so they can't be spied upon. 
See [KeePass documentation on Inner Encryption](https://keepass.info/help/kb/kdbx.html#ienc ) and 
[Process Memory Protection](https://keepass.info/help/base/security.html#secmemprot).

In summary, they are stored in an additionally secure way (additional to file security) in XML,
and they are stored in a secure way in memory.

## KeePassJava2 Approach to Protected Properties

### Up to KeePassJava2 2.2.2

All property values are stored as Strings in process memory, meaning that they can be seen via process dumps
and also, worse, Java Strings are immutable and this means that passwords stored as Strings
end up in the String pool, can't be zeroed and will be removed only when garbage collected.

I don't regard this is a *terrible* problem, but you don't have to try hard find loads 
of discussion about how *awful* it is on Google, and for sure, it's certainly not as good as it could 
be for more security conscious applications, or applications where the infrastructure
it runs on is more vulnerable. I thought [this answer](https://stackoverflow.com/a/66287347) on StackOverflow 
put it all in perspective quite well (the original question being why trying to destroy a SecretKey
raises an exception, which does seem extremely odd).

### Alternatives to Storing Passwords as Strings

You are probably at least as good as Google searches as me, good chance you may be better. So you
can read all about this there.

#### When transferring

For a start, storing the value as anything other than a String improves the situation. However,
this needs to be accompanied by providing a way of setting the value other than using a String, 
and retrieving it other than by using a String. So the first step is to enhance the
property accessor methods of the `Entry` interface to allow provision and retrieval of sensitive
values using `byte[]`, `char[]` and `CharSequence`. To be clear, the accessor methods provide the
sensitive data in a non-obfuscated form, and it's up to the caller to minimise the time
the fields contain that data before being cleared.

Note that if the provider or consumer of a password does so as a String then you're out of
luck trying to avoid strings at all. Likewise, if you collect passwords from a dialog box.

#### At rest

After the deserialization process, the passwords are stored in RAM. Therefore:

**Hashing the password is not feasible**, as the hash function is a one-way process.
This means that once a password is hashed, it cannot be reverted to its original form,
which makes hashing unsuitable in this context. Since we need to retrieve and view
the saved passwords (as per the requirements), hashing does not meet the objective.

**Encrypting and decrypting data in RAM is also problematic.** The question arises:
What key should be used for this process? If the key is stored in RAM, we are essentially creating
the same security vulnerability we are trying to avoid.

While storing data in an encrypted form may seem appealing, keeping it encrypted in the KDBX InnerStream is impractical.
This approach requires that the encrypted property values appear in the same order for both encryption and decryption, which makes it cumbersome.
Encrypting and decrypting all protected fields each time we need to access or manipulate just one of them would add significant overhead.

If the data is to be encrypted, then we need to find some way of storing the encryption key 
that is not open to simple inspection.

## KeePassJava2 2.2.3 Property Value Strategy

In the end it's up to the user of the library to decide what is the right approach to the 
trade-off between vulnerability, risk and increased resource consumption.

Because of this, KeePassJava2 provides an interface for the storage and retrieval of property values
and while it provides some default implementations, users should form
their own  opinions as to their suitability for their use cases.

### Retrieval and Storage of Property Values

Since the protected value has to be available in an unprotected form for storage and for use, it's 
up to the user to minimise the length of time that this is the case and to make sure that where
possible the data structures used are cleared after use (and as noted, in particular to avoid the use of String).

```Java
public interface PropertyValue {
    CharSequence getValue();

    char[] getValueAsChars();

    byte[] getValueAsBytes();

    boolean isProtected();

    String getValueAsString();
    // ...
}
```
It also provides an interface for the creation of values:

```Java
interface  Factory<P extends PropertyValue> {
    P of (CharSequence aCharSequence);

    P of (char [] value);

    P of (byte [] value);
}
```
which is an inner class of `Property Value`. Storage implementations will 
implement `PropertyValue` and will implement a `Factory` for their creation. They may wish 
to provide creation of the Factory as a static member on the class being created e.g.

```Java
public class SpecialPropertyValue {
    public static Factory<SpecialPropertyValue> getFactory(); 
    // ...
}

```
### Strategy Class

KeePassJava2 provides a "strategy" to determine how it stores values. It does not enforce the strategy
so a caller is free to create a protected value where one is not called for by default, or use
any implementation or strategy to store a value.

On save of the database, property values are saved to the inner stream as protected if the 
`isProtected()` method of their implementation returns `true`. On reload, they will be stored
using the class defined by the Strategy appropriate to whether they are protected or not. 

When a database is 
reloaded the default protected properties when it was saved will not be reloaded. Other than for the default properties
of `Title`, `URL`, `UserName`, `Title` and `Notes` this information can't be saved in a standardized
way in the KDBX format, and as noted below this information is ignored by the Windows KeePass implementation.

Changing the strategy doesn't alter the way that existing values are stored in the database.

**Note** KeePass (Windows) does the following: on load it loads values as protected if they
are marked as such in the database file. It resets the default protection from however it
is defined in the file to the default, 
which is to protect the `Password` property and nothing else. When saving it saves properties
that are protected in memory as protected, but additionally protects `Password` property values.

```Java
interface Strategy {
    /**
     * A list of the properties that should be protected by default
     */
    List<String> getProtectedProperties();

    /**
     * A factory for protected property values
     */
    PropertyValue.Factory<? extends PropertyValue> newProtected();

    /**
     * A factory for unprotected property values
     */
    PropertyValue.Factory<? extends PropertyValue> newUnprotected();

    /**
     * Return a factory given a property name and the properties that should be protected
     */
    default PropertyValue.Factory<? extends PropertyValue> getFactoryFor(String propertyName) {
        return getProtectedProperties().contains(propertyName) ?
                newProtected() :
                newUnprotected();
    }
}
```

#### Default Implementations of PropertyValue
There are three default implementations of `PropertyValue`: 
- `PropertyValue.StringStore` stores values as strings
- `PropertyValue.BytesStore` stores values as byte[]
- `PropertyValue.SealedStore` stores values as `javax.crypto.SealedObject` 
and stores the key using a `ByteBuffer` obtained using the `ByteBuffer.allocateDirect()` method
in order to try to have the key stored off-heap

#### Default Implementation of Strategy
`PropertyValue.Strategy.Default` defines `Password` as the only protected value and `BytesStore` and 
`SealedStore` as the unprotected and protected `PropertyValue` implementations.

### Implementation in Databases
From KeepassJava2 2.2.3 the Jackson implementation supports setting and getting of `PropertyValue`s 
from an `Entry`. 

It supports setting and getting of `Strategy` from `JacksonDatabase`.

For other database implementations `Database.supportsPropertyValueStrategy()` returns false, 
and attempts to use any methods associated with PropertyValue
cause an `UnsupportedOperationException` to be raised.







