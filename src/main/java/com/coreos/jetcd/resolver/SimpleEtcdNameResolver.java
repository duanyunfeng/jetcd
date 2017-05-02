package com.coreos.jetcd.resolver;

import io.grpc.Attributes;
import io.grpc.ResolvedServerInfo;
import io.grpc.internal.SharedResourceHolder.Resource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * SimpleEtcdNameResolver returns pre-configured addresses to the caller.
 */
public class SimpleEtcdNameResolver extends AbstractEtcdNameResolver {

  private final List<ResolvedServerInfo> servers;

  public SimpleEtcdNameResolver(String name, Resource<ExecutorService> executorResource,
      List<URI> uris) {
    super(name, executorResource);

    this.servers = Collections.unmodifiableList(
            Lists.transform(uris, new Function<URI, ResolvedServerInfo>() {

                @Override
                public ResolvedServerInfo apply(URI uri) {
                    return new ResolvedServerInfo(new InetSocketAddress(uri.getHost(), uri.getPort()),
                            Attributes.EMPTY);
                }
            })
    );
  }

  @Override
  protected List<ResolvedServerInfo> getServers() {
    return servers;
  }
}
