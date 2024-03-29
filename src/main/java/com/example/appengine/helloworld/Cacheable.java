/**
 * original author Jonathan Lurie modified by Andrew Ware
 * http://goo.gl/fx4evk
 */

package com.example.appengine.helloworld;

public interface Cacheable {

    /* By requiring all objects to determine their own expirations, the
    algorithm is abstracted from the caching service, thereby providing maximum
    flexibility since each object can adopt a different expiration strategy.
    */
    public boolean isExpired();

    /* This method will ensure that the caching service is not responsible for
    uniquely identifying objects placed in the cache.
    */
    public String getIdentifier();

}