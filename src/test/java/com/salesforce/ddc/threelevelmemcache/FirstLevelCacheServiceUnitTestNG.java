/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.ddc.threelevelmemcache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;
import com.salesforce.ddc.threelevelmemcache.exposed.strategy.CachingStrategy;

@Test(groups = "unit")
public class FirstLevelCacheServiceUnitTestNG {

    public void testStrategy() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService();
	CachingStrategy cachingStrategy = Mockito.mock(CachingStrategy.class);
	cacheService.setCachingStrategy(cachingStrategy);
	cacheService.put("a", "b");
	cacheService.append("a", "b");
	Mockito.verify(cachingStrategy, Mockito.times(2)).isCacheable("a", "b");
    }

    public void testStats() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService();
	cacheService.put("a", "b");
	Assert.assertEquals(cacheService.getStats().size(), 1);
	Assert.assertEquals(cacheService.isConnected(), true);
	Assert.assertEquals(cacheService.isSynchronousPut(), true);
    }

    public void testPut() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(1);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	Assert.assertEquals(cacheService.size(), 1);
	Assert.assertEquals(cacheService.get("a"), "b");

	cacheService.put("a2", "b2");
	Assert.assertEquals(cacheService.size(), 1);
	Assert.assertNull(cacheService.get("a"));
	Assert.assertEquals(cacheService.get("a2"), "b2");

	Mockito.verify(listener, Mockito.times(2)).put(Mockito.anyString(),
		Mockito.anyString());
	Mockito.verify(listener, Mockito.times(3)).get(Mockito.anyString());

    }

    public void testPutIfAbsent() {
	FirstLevelCacheService firstLevelCacheService = new FirstLevelCacheService();
	String key = "a";
	firstLevelCacheService.put(key, "b1");
	System.out.println(firstLevelCacheService.get(key));
	Assert.assertEquals(firstLevelCacheService.get(key) != null, true);
	firstLevelCacheService.put(key, "b2");
	System.out.println(firstLevelCacheService.get(key));
	Assert.assertEquals("b2", firstLevelCacheService.get(key));
    }

    public void testAppend() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(1);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	Assert.assertEquals(cacheService.size(), 1);
	cacheService.append("a", "c");
	Assert.assertEquals(cacheService.get("a"), "b,c");
	Assert.assertEquals(cacheService.size(), 1);
	Mockito.verify(listener).put(Mockito.anyString(), Mockito.anyString());
	Mockito.verify(listener).append(Mockito.anyString(),
		Mockito.anyString());
	Mockito.verify(listener).get(Mockito.anyString());
	Assert.assertEquals(cacheService.size(), 1);
	Mockito.verify(listener, Mockito.times(0)).remove(Mockito.anyString());
    }

    public void testRemove() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(1);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	Assert.assertEquals(cacheService.size(), 1);
	cacheService.remove("a");
	Assert.assertNull(cacheService.get("a"));
	Assert.assertEquals(cacheService.size(), 0);
	Mockito.verify(listener).put(Mockito.anyString(), Mockito.anyString());
	Mockito.verify(listener).get(Mockito.anyString());
	Mockito.verify(listener).remove(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
		Mockito.anyString());
    }

    public void testGet() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(1);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	Assert.assertEquals(cacheService.get("a"), "b");
	Assert.assertEquals(cacheService.size(), 1);
	Mockito.verify(listener).put(Mockito.anyString(), Mockito.anyString());
	Mockito.verify(listener).get(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).remove(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
		Mockito.anyString());

	Assert.assertNull(cacheService.get(null),
		"get null did not return null");
    }

    public void testGetBatch() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(2);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	cacheService.put("a2", "b2");
	Assert.assertEquals(cacheService.getBatch(Arrays.asList("a", "a2")),
		Arrays.asList("b", "b2"));
	Mockito.verify(listener, Mockito.times(2)).put(Mockito.anyString(),
		Mockito.anyString());
	Mockito.verify(listener, Mockito.times(2)).get(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).remove(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
		Mockito.anyString());
    }

    public void testPutBatch() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(2);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	Map map = new HashMap<Object, Serializable>();
	map.put("a", "b");
	map.put("a2", "b2");
	cacheService.putBatch(map);
	Assert.assertEquals(cacheService.getBatch(Arrays.asList("a", "a2")),
		Arrays.asList("b", "b2"));
	Mockito.verify(listener, Mockito.times(2)).put(Mockito.anyString(),
		Mockito.anyString());
	Mockito.verify(listener, Mockito.times(2)).get(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).remove(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
		Mockito.anyString());
    }

    public void testRemoveBatch() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(2);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	Map map = new HashMap<Object, Serializable>();
	map.put("a", "b");
	map.put("a2", "b2");
	cacheService.putBatch(map);
	cacheService.removeBatch((Collection) Arrays.asList("a", "a2"));
	Assert.assertEquals(cacheService.size(), 0);
	Mockito.verify(listener, Mockito.times(2)).put(Mockito.anyString(),
		Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).get(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(2)).remove(Mockito.anyString());
	Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
		Mockito.anyString());
    }

    public void testShutdown() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService(2);
	CacheListener listener = Mockito.mock(CacheListener.class);
	cacheService.setListener(listener);
	cacheService.put("a", "b");
	cacheService.shutdown();
	Assert.assertEquals(cacheService.size(), 0);
    }

    public void testIncrDecr() {
	FirstLevelCacheService cacheService = new FirstLevelCacheService();
	cacheService.put("a", "1");
	cacheService.put("b", "1");
	cacheService.put("c", "1");
	Assert.assertEquals(cacheService.incr("a"), -1);
	Assert.assertEquals(cacheService.decr("b"), -1);
	Assert.assertEquals(cacheService.add("c", "1"), true);
	Assert.assertEquals(cacheService.size(), 0);
	Assert.assertEquals(cacheService.get("a"), null);
	Assert.assertEquals(cacheService.get("b"), null);
	Assert.assertEquals(cacheService.get("c"), null);
	cacheService.put("a", "1");
	cacheService.put("b", "1");
	Assert.assertEquals(cacheService.incr("a", 1, 0, 0), -1);
	Assert.assertEquals(cacheService.decr("b", 1, 0, 0), -1);
	Assert.assertEquals(cacheService.size(), 0);
	Assert.assertEquals(cacheService.get("a"), null);
	Assert.assertEquals(cacheService.get("b"), null);
    }

}
