package io.pdef;

import com.google.common.collect.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class Descriptors {
	private Descriptors() {}

	public static Descriptor<Boolean> bool = new Descriptor<Boolean>() {
		@Override
		public Class<Boolean> getJavaClass() {
			return boolean.class;
		}

		@Override
		public Boolean getDefault() {
			return false;
		}

		@Override
		public Boolean parse(final Object object) {
			return object == null ? false : (Boolean) object;
		}

		@Override
		public Boolean serialize(final Boolean object) {
			return object == null ? false : object;
		}
	};

	public static Descriptor<Short> int16 = new Descriptor<Short>() {
		@Override
		public Class<Short> getJavaClass() {
			return short.class;
		}

		@Override
		public Short getDefault() {
			return (short) 0;
		}

		@Override
		public Short parse(final Object object) {
			return object == null ? 0 : ((Number) object).shortValue();
		}

		@Override
		public Short serialize(final Short object) {
			return object == null ? (short) 0 : object;
		}
	};

	public static Descriptor<Integer> int32 = new Descriptor<Integer>() {
		@Override
		public Class<Integer> getJavaClass() {
			return int.class;
		}

		@Override
		public Integer getDefault() {
			return 0;
		}

		@Override
		public Integer parse(final Object object) {
			return object == null ? 0 : ((Number) object).intValue();
		}

		@Override
		public Integer serialize(final Integer object) {
			return object == null ? 0 : object;
		}
	};

	public static Descriptor<Long> int64 = new Descriptor<Long>() {
		@Override
		public Class<Long> getJavaClass() {
			return long.class;
		}

		@Override
		public Long getDefault() {
			return 0L;
		}

		@Override
		public Long parse(final Object object) {
			return object == null ? 0 : ((Number) object).longValue();
		}

		@Override
		public Long serialize(final Long object) {
			return object == null ? 0L : object;
		}
	};

	public static Descriptor<Float> float0 = new Descriptor<Float>() {
		@Override
		public Class<Float> getJavaClass() {
			return float.class;
		}

		@Override
		public Float getDefault() {
			return 0f;
		}

		@Override
		public Float parse(final Object object) {
			return object == null ? 0 : ((Number) object).floatValue();
		}

		@Override
		public Float serialize(final Float object) {
			return object == null ? 0f : object;
		}
	};

	public static Descriptor<Double> double0 = new Descriptor<Double>() {
		@Override
		public Class<Double> getJavaClass() {
			return double.class;
		}

		@Override
		public Double getDefault() {
			return 0d;
		}

		@Override
		public Double parse(final Object object) {
			return object == null ? 0 : ((Number) object).doubleValue();
		}

		@Override
		public Double serialize(final Double object) {
			return object == null ? 0d : object;
		}
	};

	public static Descriptor<String> string = new Descriptor<String>() {
		@Override
		public Class<String> getJavaClass() {
			return String.class;
		}

		@Override
		public String getDefault() {
			return null;
		}

		@Override
		public String parse(final Object object) {
			return (String) object;
		}

		@Override
		public String serialize(final String object) {
			return object;
		}
	};

	public static Descriptor<Void> void0 = new Descriptor<Void>() {
		@Override
		public Class<Void> getJavaClass() {
			return void.class;
		}

		@Override
		public Void getDefault() {
			return null;
		}

		@Override
		public Void parse(final Object object) {
			return null;
		}

		@Override
		public Void serialize(final Void object) {
			return null;
		}
	};

	public static Descriptor<Object> object = new Descriptor<Object>() {
		@Override
		public Class<Object> getJavaClass() {
			return Object.class;
		}

		@Override
		public Object getDefault() {
			return null;
		}

		@Override
		public Object parse(final Object object) {
			return object;
		}

		@Override
		public Object serialize(final Object object) {
			return Descriptors.object;
		}
	};

	public static <T> Descriptor<T> enum0(final T defaultValue) {
		return null;
	}

	public static <T> Descriptor<List<T>> list(final Descriptor<T> element) {
		checkNotNull(element);
		return new Descriptor<List<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<List<T>> getJavaClass() {
				return (Class) List.class;
			}

			@Override
			public List<T> getDefault() {
				return ImmutableList.of();
			}

			@Override
			public List<T> parse(final Object object) {
				if (object == null) return ImmutableList.of();

				List<?> list = (List<?>) object;
				List<T> result = Lists.newArrayList();
				for (Object e : list) {
					T r = element.parse(e);
					result.add(r);
				}

				return ImmutableList.copyOf(result);
			}

			@Override
			public List<Object> serialize(final List<T> object) {
				if (object == null) return null;

				List<Object> result = Lists.newArrayList();
				for (T e : object) {
					result.add(element.serialize(e));
				}

				return result;
			}
		};
	}

	public static <T> Descriptor<Set<T>> set(final Descriptor<T> element) {
		checkNotNull(element);
		return new Descriptor<Set<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<Set<T>> getJavaClass() {
				return (Class) Set.class;
			}

			@Override
			public Set<T> getDefault() {
				return ImmutableSet.of();
			}

			@Override
			public Set<T> parse(final Object object) {
				if (object == null) return ImmutableSet.of();

				List<?> list = (List<?>) object;
				List<T> result = Lists.newArrayList();
				for (Object e : list) {
					T r = element.parse(e);
					result.add(r);
				}

				return ImmutableSet.copyOf(result);
			}

			@Override
			public Set<Object> serialize(final Set<T> object) {
				if (object == null) return null;

				Set<Object> result = Sets.newHashSet();
				for (T e : object) {
					result.add(element.serialize(e));
				}

				return result;
			}
		};
	}

	public static <K, V> Descriptor<Map<K, V>> map(final Descriptor<K> key,
			final Descriptor<V> value) {
		checkNotNull(key);
		checkNotNull(value);
		return new Descriptor<Map<K, V>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<Map<K, V>> getJavaClass() {
				return (Class) Map.class;
			}

			@Override
			public Map<K, V> getDefault() {
				return ImmutableMap.of();
			}

			@Override
			public Map<K, V> parse(final Object object) {
				if (object == null) return ImmutableMap.of();

				Map<?, ?> map = (Map<?, ?>) object;
				Map<K, V> result = Maps.newHashMap();
				for (Map.Entry<?, ?> e : map.entrySet()) {
					K k = key.parse(e.getKey());
					V v = value.parse(e.getValue());
					result.put(k, v);
				}

				return ImmutableMap.copyOf(result);
			}

			@Override
			public Map<Object, Object> serialize(final Map<K, V> object) {
				if (object == null) return null;

				Map<Object, Object> result = Maps.newHashMap();
				for (Map.Entry<K, V> e : object.entrySet()) {
					Object k = key.serialize(e.getKey());
					Object v = value.serialize(e.getValue());
					result.put(k, v);
				}

				return result;
			}
		};
	}
}
