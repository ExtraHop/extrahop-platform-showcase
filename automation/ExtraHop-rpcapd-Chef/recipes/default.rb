#
# Cookbook Name:: ExtraHop-rpcapd
# Recipe:: rpcapd
#
# Copyright 2016, ExtraHop Networks
#
# All rights reserved - Do Not Redistribute

case[node[:platform],node[:kernel][:machine]]
	when ["ubuntu","x86_64"],["centos","x86_64"],["amazon","x86_64"]
		# download rpcapd from ExtraHop node 
		execute 'download_rpcapd_linux' do
			command "curl -OkL 'https://#{node[:extrahop][:host]}/tools/rpcapd.tar.gz'"
			cwd "/tmp/"
		action :run
		end
		
		# unpack downloaded tar file
		execute 'unpack_rpcapd' do
			command "tar xf rpcapd.tar.gz"
			cwd "/tmp/"
		action :run
		end
		
		# install rpcapd
		execute 'install_rpcapd_linux' do
			command "cd /tmp/rpcapd && sudo ./install.sh #{node[:extrahop][:host]} #{node[:extrahop][:port]}"
		action :run
		end

		# remove rpacpd tar file
		execute 'remove_rpcapd_archive' do
			command "rm rpcapd.tar.gz && rm -rf rpcapd"
			cwd "/tmp/"
		action :run
		end
	
	when ["windows","x86_64"],["windows","x64-mingw32"]
		# download powershell rpcapd script from ExtraHop node
		powershell_script 'download_rpcapd_windows' do
			code <<-EOH
			$download_url = "http://#{node[:extrahop][:host]}/tools/install-rpcapd.ps1"
			(New-Object System.Net.WebClient).DownloadFile($download_url, 'install-rpcapd.ps1')
			EOH
		action :run
		end

		# run powershell install script
		powershell_script 'install_rpcapd_windows' do 
			code "./install-rpcapd.ps1 #{node[:extrahop][:host]} #{node[:extrahop][:host]}" 
		action :run
		end

		# remove powershell install script
		powershell_script 'remove_rpcapd_installer' do
			code "Remove-Item install-rpcapd.ps1"
                action :run
		end
end
