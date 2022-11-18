package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MultiAccessComponents<K, C> extends ConcurrentSkipListMap<K, C> {
	private static final long serialVersionUID = 1L;
	private ConcurrentMap<String, K> uriToKeyMap = new ConcurrentHashMap<>();
	private ConcurrentMap<K, String> keyToUriMap = new ConcurrentHashMap<>();


	public C getByUri(String uri) {
		return this.get(uriToKeyMap.get(uri));
	}

	public C removeByUri(String uri) {
		if (uriToKeyMap.containsKey(uri)) {
			K key = uriToKeyMap.get(uri);
			removeUriMapping(uri);
			return this.remove(key);
		}
		return null;
	}
	
	@Override
	public C remove(Object key) {
		C component = super.remove(key);
		removeKeyMapping(key);
		return component;
	}

	public K putUriMapping(String uri, K key) {
		keyToUriMap.put(key, uri);
		return uriToKeyMap.put(uri, key);
	}

	private void removeUriMapping(String uri) {
		K key = uriToKeyMap.remove(uri);
		if (key != null) {
			removeKeyMapping(key);
		}
	}

	private void removeKeyMapping(Object key) {
		String uri = keyToUriMap.remove(key);
		if (uri != null) {
			removeUriMapping(uri);
		}
	}

}
