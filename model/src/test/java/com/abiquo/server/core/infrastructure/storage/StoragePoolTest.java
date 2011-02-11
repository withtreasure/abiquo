  package com.abiquo.server.core.infrastructure.storage;

  import com.abiquo.server.core.common.DefaultEntityTestBase;
  import com.softwarementors.bzngine.entities.test.InstanceTester;

  public class StoragePoolTest extends DefaultEntityTestBase<StoragePool>
  {

      @Override
      protected InstanceTester<StoragePool> createEntityInstanceGenerator()
      {
          return new StoragePoolGenerator(getSeed());
      }
  }
