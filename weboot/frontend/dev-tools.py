import os, subprocess as sp, pathlib as pl, sys, shutil, re


def get_next_vers(major, minor):
    if major >= 10_000:
        return (0,0)
    if minor >= 10_000:
        major += 1
        minor = 0
    else:
        minor += 1
    return (major, minor)



def main():
    step = 1
    ## print current working dir
    print(f"[{step}]cwd is: {os.getcwd()}")
    step+=1
    os.chdir(pl.Path('../../'))
    print(f"\n[{step}]building weboot jar...pls wait")
    step+=1
    comp_proc = sp.run(['gradle', 'clean', ':weboot:bootJar'], capture_output=True)
    if comp_proc.returncode != 0:
        sys.exit(str(comp_proc.stderr))

    print(f"\n[{step}]weboot jar build SUCCESSFULL!")
    step+=1
    os.chdir(pl.Path('./weboot/frontend/'))
    print(f"\n[{step}]cwd is: {os.getcwd()}")
    step+=1

    print(f"\n[{step}]now building the react assets")
    step+=1
    comp_proc = sp.run(['webpack', '--config', 'webpack.config.js'], capture_output=True)
    if comp_proc.returncode != 0:
        sys.exit(str(comp_proc.stderr))
    print(f"\n[{step}]react assets successfully built!")
    step+=1

    weboot_jar_path = pl.Path('../../../zencode-build/weboot-jar/')
    react_assets_path = pl.Path('../../../zencode-build/react-assets/')

    if not weboot_jar_path.exists():
        ## use makedirs to make this directory
        print(f"\n[{step}]making the weboot jar path...")
        step+=1
        os.makedirs(weboot_jar_path)

    if not react_assets_path.exists():
        ## use makedirs to make this directory
        print(f"\n[{step}]making the react asset path...")
        step+=1
        os.makedirs(react_assets_path)
    else:
        shutil.rmtree(react_assets_path)
        print(f"\n[{step}]cleaned up the whole react assets tree.")
        step+=1

        print(f"\n[{step}]making the react asset path from scratch...")
        step+=1
        os.makedirs(react_assets_path)


    ## move the jar and assets to target git repo
    origin_weboot_jar = pl.Path('../build/libs/weboot.jar')
    shutil.copy2(origin_weboot_jar, weboot_jar_path / 'weboot.jar')
    print(f"\n[{step}]moved the weboot jar into the target repo")
    step+=1


    origin_react_assets = pl.Path('./dist/')
    for asset in origin_react_assets.iterdir():
        shutil.move(asset, react_assets_path)
    print(f"\n[{step}]moved all the react assets to target repo")
    step+=1

    ## move to the target repo
    target_repo = pl.Path('../../../zencode-build/')
    os.chdir(target_repo)
    print(f"\n[{step}]moved cwd to target repo...")
    step+=1

    comp_proc = sp.run(['git', 'add', '.'], capture_output=True)
    if comp_proc.returncode != 0:
        sys.exit(comp_proc.stderr)
    print(f"\n[{step}]added latest changes  in zencode-build to staging area...")
    step+=1

    comp_proc = sp.run(['git', 'show', '-s', '--format=%B'], capture_output=True, text=True)
    if comp_proc.returncode != 0:
        sys.exit(comp_proc.stderr)

    commit_msg = str(comp_proc.stdout)
    print(f"\n[{step}]curr commit msg is: {commit_msg}")
    step+=1
    pattern = r"(\[\d+\])(\[\d+\])"
    match = re.search(pattern, commit_msg)
    major_num_str = match.group(1)
    major_num = int(major_num_str[1])
    minor_num_str = match.group(2)
    minor_num = int(minor_num_str[1])
    new_major, new_minor = get_next_vers(major_num, minor_num)
    new_commit_msg = f"build version [{new_major}][{new_minor}]"

    print(f"\n[{step}]new commit msg is: {new_commit_msg}")
    step+=1

    comp_proc = sp.run(['git', 'commit', '-m', f'{new_commit_msg}'], capture_output=True)
    if comp_proc.returncode != 0:
        sys.exit(comp_proc.stderr)
    print(f"\n[{step}]commited to target repo on branch main!")
    step+=1

    comp_proc = sp.run(['git', 'push', 'origin', 'main'], capture_output=True)
    if comp_proc.returncode != 0:
        sys.exit(comp_proc.stderr)
    print(f"\n[{step}]pushed the latest build to the remote repo!")
    step+=1

    print(f"\n[{step}]process complete. Now run the script in the ec2 instance!")
    step+=1


if __name__ == '__main__':
    main()
