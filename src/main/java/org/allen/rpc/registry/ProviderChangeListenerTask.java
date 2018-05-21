package org.allen.rpc.registry;

import org.allen.util.ZkPathParseUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Zhou Zhengwen
 */
public class ProviderChangeListenerTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProviderChangeListenerTask.class);

    public boolean exitFlag = false;

    private CuratorFramework client;

    private Map<Class<?>, List<ProviderLocation>> providerLocationsConfig;

    public ProviderChangeListenerTask(CuratorFramework client, Map<Class<?>, List<ProviderLocation>> providerLocationsConfig) {
        this.client = client;
        this.providerLocationsConfig = providerLocationsConfig;
    }

    @Override
    public void run() {
        for (Map.Entry<Class<?>, List<ProviderLocation>> entry : providerLocationsConfig.entrySet()) {
            Class<?> consumerInterface = entry.getKey();
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/myrpc/" + consumerInterface.getName() + "/providers", true);
            pathChildrenCache.getListenable().addListener((client, event) -> {
                ChildData child = event.getData();
                String path = child.getPath();
                List<ProviderLocation> providerLocations = entry.getValue();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        logger.info("has listened a child node added: {}", path);
                        ProviderLocation newAddedProviderLocation = ZkPathParseUtil.parseProviderPath(path);
                        providerLocations.add(newAddedProviderLocation);
                        break;
                    case CHILD_REMOVED:
                        logger.info("has listened a child node removed: {}", path);
                        ProviderLocation removedProviderLocation = ZkPathParseUtil.parseProviderPath(path);
                        providerLocations.removeIf(providerLocation -> providerLocation.equals(removedProviderLocation));
                        break;
                }
            });
            try {
                pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            } catch (Exception e) {
                logger.error("Error in starting path children listener", e);
            }
        }
        while (!exitFlag) {
//            logger.info("listening to zookeeper");
        }
    }
}
