package com.polopoly.jenkins.plugin.offlineonfailure;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.slaves.OfflineCause;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Plugin that will take a node offline immediately after
 * a job has reported FAILURE.
 */
public class OfflineOnFailurePublisher
    extends Notifier
{
    @DataBoundConstructor
    public OfflineOnFailurePublisher() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build,
                           Launcher launcher,
                           BuildListener listener)
        throws InterruptedException,
               IOException
    {
        if (build.getResult().isWorseOrEqualTo(Result.FAILURE)) {

            Node buildNode = build.getBuiltOn();
            PrintStream log = listener.getLogger();

            // Never set master offline
            if (Hudson.getInstance() != buildNode) {
                buildNode.toComputer().setTemporarilyOffline(true, OfflineCause.create(Messages._OfflineOnFailureCause_Description()));
                log.println(Messages.OfflineOnFailure_FailureDetected());
            } else {
                log.println(Messages.OfflineOnFailure_FailureDetectedOnMaster());
            }
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.BUILD;
    }

    /*
    @Override
    public DescriptorImpl getDescriptor()
    {
        return (DescriptorImpl) super.getDescriptor();
    }
    */

    /**
     * Descriptor for {@link OfflineOnFailurePublisher}.
     */
    @Extension
    public static final class DescriptorImpl
        extends BuildStepDescriptor<Publisher>
    {
        public DescriptorImpl()
        {
            super(OfflineOnFailurePublisher.class);
        }

        public String getDisplayName()
        {
            return "Take node offline on failure";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType)
        {
            return true;
        }

        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData)
            throws FormException
        {
            return req.bindJSON(OfflineOnFailurePublisher.class, formData);
        }
    }
}
