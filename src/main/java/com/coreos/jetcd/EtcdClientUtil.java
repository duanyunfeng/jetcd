package com.coreos.jetcd;

import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;
import io.grpc.stub.AbstractStub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executor;

import com.coreos.jetcd.resolver.SimpleEtcdNameResolverFactory;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public final class EtcdClientUtil {

  private EtcdClientUtil() {
  }

  /**
   * add token to channel's head.
   *
   * @param stub the stub to attach head
   * @param token the token for auth
   * @param <T> the type of stub
   * @return the attached stub
   */
  static final <T extends AbstractStub<T>> T configureStub(final T stub, Optional<String> token) {
      return token.transform(new Function<String, T>() {

          @Override
          public T apply(String token) {
              final Metadata metadata = new Metadata();
              Metadata.Key<String> TOKEN_KEY = Metadata.Key
                  .of(EtcdConstants.TOKEN, Metadata.ASCII_STRING_MARSHALLER);
              metadata.put(TOKEN_KEY, token);
              CallCredentials callCredentials = new CallCredentials() {
                  
                  @Override
                  public void applyRequestMetadata(MethodDescriptor<?, ?> method, Attributes attrs, Executor appExecutor,
                          MetadataApplier metadataApplier) {
                      metadataApplier.apply(metadata);
                  }
              }; 
              return stub.withCallCredentials(callCredentials);
          }
          
      }).or(stub);
  }

  static final NameResolver.Factory simpleNameResolveFactory(List<String> endpoints) {
      return new SimpleEtcdNameResolverFactory(
              Lists.transform(endpoints, new Function<String, URI>() {

                  @Override
                  public URI apply(String endpoint) {
                      return EtcdClientUtil.endpointToUri(endpoint);
                  }
              })
      );
  }

  static URI endpointToUri(String endpoint) {
    try {
      if (!endpoint.startsWith("http://")) {
        endpoint = "http://" + endpoint;
      }
      return new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static ManagedChannelBuilder<?> defaultChannelBuilder(NameResolver.Factory factory) {
    return ManagedChannelBuilder.forTarget("etcd")
        .nameResolverFactory(factory)
        .usePlaintext(true);
  }
}
