/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.osgi.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Report OSGi events as they happen. Listen for BundleEvent, ServiceEvent and
 * FrameworkEvent types.
 * 
 * If the logging level is set to debug, format all of these events and write
 * them to the log. Otherwise, do nothing.
 */
public class OsgiFrameworkEventMonitor implements ServiceListener,
		BundleListener, FrameworkListener {
	private static final Log log = LogFactory
			.getLog(OsgiFrameworkEventMonitor.class);

	public void addToContext(BundleContext bundleContext) {
		bundleContext.addBundleListener(this);
		bundleContext.addServiceListener(this);
		bundleContext.addFrameworkListener(this);
	}

	@Override
	public void bundleChanged(BundleEvent be) {
		if (log.isDebugEnabled()) {
			log.debug(format(be));
		}
	}

	@Override
	public void serviceChanged(ServiceEvent se) {
		if (log.isDebugEnabled()) {
			log.debug(format(se));
		}
	}

	@Override
	public void frameworkEvent(FrameworkEvent fe) {
		if (log.isDebugEnabled()) {
			log.debug(format(fe));
		}
	}

	private String format(BundleEvent be) {
		BundleEventType type = BundleEventType.fromCode(be.getType());
		String sourceName = format(be.getBundle());
		String originName = format(be.getOrigin());

		if (type == BundleEventType.INSTALLED) {
			return "BundleEvent[" + type + ", " + sourceName + " in "
					+ originName + "]";
		} else {
			return "BundleEvent[" + type + ", " + sourceName + "]";
		}

	}

	private String format(ServiceEvent se) {
		ServiceEventType type = ServiceEventType.fromCode(se.getType());
		String bundleName = "[-] no bundle";
		String serviceName = "no service";

		ServiceReference<?> ref = se.getServiceReference();
		if (ref != null) {
			Bundle bundle = ref.getBundle();
			if (bundle != null) {
				bundleName = format(bundle);
				BundleContext bc = bundle.getBundleContext();
				if (bc != null) {
					Object service = bc.getService(ref);
					if (service != null) {
						serviceName = service.getClass().getName();
					}
				}
			}
		}
		return "ServiceEvent[" + type + ", " + serviceName + ", " + bundleName
				+ "]";
	}

	private String format(FrameworkEvent fe) {
		FrameworkEventType type = FrameworkEventType.fromCode(fe.getType());
		String cString = String.valueOf(fe.getThrowable());
		String bString = format(fe.getBundle());
		return "FrameworkEvent[" + type + ", " + bString + ", " + cString + "]";
	}

	private String format(Bundle b) {
		if (b == null) {
			return "[-] no bundle";
		} else {
			return "[" + b.getBundleId() + "] " + b.getSymbolicName();
		}
	}

	private enum BundleEventType {
		INSTALLED(1), //
		LAZY_ACTIVATION(512), //
		RESOLVED(32), //
		STARTED(2), //
		STARTING(128), //
		STOPPED(4), //
		STOPPING(256), //
		UNINSTALLED(16), //
		UNRESOLVED(64), //
		UPDATED(8); //

		private final int code;

		BundleEventType(int code) {
			this.code = code;
		}

		static BundleEventType fromCode(int code) {
			for (BundleEventType type : BundleEventType.values()) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
	}

	private enum ServiceEventType {
		MODIFIED(2), //
		MODIFIED_ENDMATCH(8), //
		REGISTERED(1), //
		UNREGISTERING(4); //

		private final int code;

		ServiceEventType(int code) {
			this.code = code;
		}

		static ServiceEventType fromCode(int code) {
			for (ServiceEventType type : ServiceEventType.values()) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
	}

	private enum FrameworkEventType {
		ERROR(2), //
		INFO(32), //
		PACKAGES_REFRESHED(4), //
		STARTED(1), //
		STARTLEVEL_CHANGED(8), //
		STOPPED(64), //
		STOPPED_BOOTCLASSPATH_MODIFIED(256), //
		STOPPED_UPDATE(128), //
		WAIT_TIMEDOUT(512), //
		WARNING(16); //

		private final int code;

		FrameworkEventType(int code) {
			this.code = code;
		}

		static FrameworkEventType fromCode(int code) {
			for (FrameworkEventType type : FrameworkEventType.values()) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
	}

}
