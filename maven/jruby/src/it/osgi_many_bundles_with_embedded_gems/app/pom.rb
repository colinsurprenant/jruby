#-*- mode: ruby -*-

packaging 'bundle'

properties( 'tesla.dump.pom' => 'pom.xml',
            'exam.version' => '3.0.3',
            'url.version' => '1.5.2',
            'logback.version' => '1.0.13',
            'jruby.version' => '@project.version@' )

pom 'org.jruby:jruby', '${jruby.version}'

plugin( 'org.apache.felix:maven-bundle-plugin', '2.4.0',
        #:excludeDependencies => 'bundle',
        :instructions => {
          # org.junit is needed for the test phase to run unit tests
          'Export-Package' => 'org.jruby.*,org.junit.*',
          # this is needed to find javax.* packages
          'DynamicImport-Package' => '*',
          'Include-Resource' => '{maven-resources}',
          'Import-Package' => '!org.jruby.*,*;resolution:=optional, org.jruby.osgi.gems, org.jruby.osgi.scripts',
          'Embed-Dependency' => '*;type=jar;scope=compile|runtime;inline=true',
          'Embed-Transitive' => true
        } ) do
  # pack the bundle before the test phase
  execute_goal :bundle, :phase => 'process-test-resources'
  # TODO fix DSL
  @current.extensions = true
end

# the tests run inside the bundle so we need it as dependency for the bundle
jar 'junit:junit:4.11'
bundle 'org.jruby.osgi:gems-bundle', '1.0'
bundle 'org.jruby.osgi:scripts-bundle', '1.0'

scope :test do
  jar 'org.osgi:org.osgi.core:5.0.0'

  jar 'org.ops4j.pax.exam:pax-exam-link-mvn', '${exam.version}'
  jar 'org.ops4j.pax.exam:pax-exam-junit4', '${exam.version}'
  jar 'org.ops4j.pax.exam:pax-exam-container-forked', '${exam.version}'
  jar 'org.ops4j.pax.url:pax-url-aether', '${url.version}'

  jar 'ch.qos.logback:logback-core', '${logback.version}'
  jar 'ch.qos.logback:logback-classic', '${logback.version}'

  #jar 'org.eclipse.osgi:org.eclipse.osgi:3.6.0.v20100517'
  #jar 'org.eclipse.osgi:org.eclipse.osgi:3.7.1'
  jar 'org.apache.felix:org.apache.felix.framework:4.4.1'
end
