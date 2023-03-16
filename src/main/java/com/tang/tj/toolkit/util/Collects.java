package com.tang.tj.toolkit.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Collects 
 * 单线程集合操作
 * 
 * @author tmy
 * @date 2022-04-22 10:17
 */
public class Collects {

	@SafeVarargs
	public static <T> boolean isIn(T item, T...values) {
		if (values == null || values.length == 0) {
			return false;
		}
		for (T value : values) {
			if (Objects.equals(item, value)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static boolean isEmpty(Object[] arr) {
		return arr == null || arr.length == 0;
	}

	public static boolean isNotEmpty(Object[] arr) {
		return !isEmpty(arr);
	}

	public static <T, K> Map<K, T> collect2Map(Collection<T> collect,
                                               Function<T, K> keyProvider) {
		return collect2Map(collect, keyProvider, new HashMap<>());
	}

	public static <T, K> Map<K, T> collect2Map(Collection<T> collect,
                                               Function<T, K> keyProvider,
                                               Map<K, T> resultMap) {
		for (T item : collect) {
			resultMap.put(keyProvider.apply(item), item);
		}
		return resultMap;
	}

	public static <T, K, V> Map<K, V> collect2KvMap(Collection<T> collect,
                                                    Function<T, K> keyProvider,
                                                    Function<T, V> valueProvider) {
		return collect2KvMap(collect, keyProvider, valueProvider, new HashMap<>());
	}

	public static <T, K, V> Map<K, V> collect2KvMap(Collection<T> collect,
                                                    Function<T, K> keyProvider,
                                                    Function<T, V> valueProvider,
                                                    Map<K, V> resultMap) {
		for (T item : collect) {
			resultMap.put(keyProvider.apply(item), valueProvider.apply(item));
		}
		return resultMap;
	}

	public static <T,K,V> Map<K, List<V>> group(Collection<T> collect,
                                                Function<T, K> groupBy,
                                                Function<T, V> valBy) {
		return group(3, collect, groupBy, valBy);
	}

    /**
     * 对 list 数据进行分组
     * @param aboutFactorOfSize 集合数据条数约是分组后数据的n倍
     * @param collect 集合数据
     * @param groupBy 集合元素分组值
     * @param valBy 分组结果
     * @param <T> 集合元素类型
     * @param <K> 分组值类型
     * @param <V> 分组结果类型
     * @return 分组结果
     */
	public static <T,K,V> Map<K, List<V>> group(int aboutFactorOfSize,
                                                Collection<T> collect,
                                                Function<T, K> groupBy,
                                                Function<T, V> valBy) {
		if (aboutFactorOfSize < 1) {
			throw new IllegalArgumentException("aboutFactorOfSize需大于0");
		}
		if (collect == null || collect.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<K, List<V>> result = new HashMap<>(Math.max(6, Math.min(16, collect.size() / aboutFactorOfSize)));
		for (T item : collect) {
			K key = groupBy.apply(item);
			V val = valBy.apply(item);
			List<V> vals = result.computeIfAbsent(key, k -> new LinkedList<>());
			vals.add(val);
		}
		return result;
	}

	/**
	 * reduce 方法接收一个函数作为累加器，数组中的每个值（从左到右）开始缩减，最终计算为一个值
	 *
	 * @param collection   被计算的集合
	 * @param function     函数累加器
	 * @param initialState 初始值
	 * @param <T>          被计算集合的类型
	 * @param <R>          返回类型，最终值类型
	 * @return 最后计算的值
	 */
	public static <T, R> R reduce(Collection<T> collection, R initialState, BiFunction<R, T, R> function) {
		for (T element : collection) {
			initialState = function.apply(initialState, element);
		}
		return initialState;
	}

	/**
	 * 数组翻转
	 */
	public static long[] revers(long[] arr) {
		for (int i = 0, j = arr.length - 1; i < j; i++, j--) {
			long temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
		}
		return arr;
	}

	/**
	 * 根据元素个数计算 map 容量大小
	 * @param size 元素个数
	 * @return map 容量大小
	 */
	public static int mapCapacity(int size) {
		return Math.max(2, (int) Math.ceil(size / 0.75));
	}


	/**
	 * 从给定的集合中找到匹配的第一个元素，找不到返回 null
	 *
	 * @param collection 给定的集合
	 * @param predicate  Predicate lambda表达式
	 * @param <T>        集合元素类型
	 * @return 找到的元素
	 */
	public static <T> T findOne(Collection<T> collection, Predicate<T> predicate) {
		for (T element : collection) {
			if (predicate.test(element)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 将参数转换成 Map
	 * key为空跳过键值对
	 * @param kvs key1, val1, key2, val2, ....
	 */
	public static Map<String, Object> asMap(Object...kvs) {
		if ((kvs.length % 2) != 0) {
			throw new IllegalArgumentException("参数个数需为偶数");
		}
		Map<String, Object> map = new HashMap<>(mapCapacity(kvs.length / 2));
		for (int i = 0; i < kvs.length; i+=2) {
			if (kvs[i] == null) {
				continue;
			}
			map.put(String.valueOf(kvs[i]), kvs[i+1]);
		}
		return map;
	}

	/**
	 * 映射集合元素
	 */
	public static <T, R> List<R> mapping(Collection<T> datas, Function<T, R> mappingFunc) {
		if (isEmpty(datas)) {
			return new ArrayList<>(2);
		}
		List<R> results = new ArrayList<>(datas.size());
		for (T data : datas) {
			results.add(mappingFunc.apply(data));
		}
		return results;
	}

}
