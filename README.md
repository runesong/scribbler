# Scribbler

To run the app, you will need to create a local trust store with a self-signed certificate

**Step 1:** Create the server cert in a key store.

```
keytool -alias scribbler -keystore keystore \
    -storepass password -keypass password \
    -genkeypair -keyalg RSA -dname CN=localhost
```

**Step 2:** Export the server cert from the keystore.

```
keytool -alias scribbler -keystore keystore \
    -storepass password -export -file server.cert
```

**Step 3:** Import the server cert into a trust store.
```
keytool -alias scribbler -keystore truststore \
    -storepass password -keypass password \
    -import -trustcacerts -file server.cert
```

