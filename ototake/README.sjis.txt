EDCW2012 �O�u���E������茟�o�v���O���� README
���� �k�l �iOTOTAKE Hokuto�j
2012/08/09�F�V�K�쐬

========================
 �T�v
========================

  �{�V�X�e���́CError Detection and Correction Workshop 2012 �iEDCW2012�j
�ɂ�����O�u���E�����g���b�N�̌�茟�o��ړI�Ƃ������̂ł��D
�O�u���E�����Ƃ��ɁC�P��̃v���O�����Ō�茟�o���s���܂��D

  �{�V�X�e����C#�ɂ���ď�����Ă��܂��D
�������Windows�����Linux�ŁC�ȉ��̊��ɂē��삪����ɍs���邱�Ƃ��m�F���Ă��܂��D
  �EWindows 7 x64 with .NET Framework 4.0
  �EUbuntu Server 12.04 x64 with Mono 2.10.8.1

  �ȍ~�̐����ł́C�{�V�X�e���̃��[�g�f�B���N�g����
    {project_root}/
  �ƋL�q���܂��D


=========================================
 �K�v�ȃT�[�h�p�[�e�B�̃��W���[���E���C�u����
=========================================

  �{�V�X�e�����r���h����O�ɁC�������̃T�[�h�p�[�e�B���W���[������������K�v������܂��D
�ȉ��ɕK�v�ȃ��W���[���̏ڍׂɂ��ďq�ׂ܂��D


CLR �܂��͂��̌݊������^�C��
--------------------------
  Windows�̏ꍇ�́CWindows Update������ .NET Framework 4.0�ȏ���C���X�g�[�����Ă��������D
  Linux�̏ꍇ�́CMono (http://www.mono-project.com/) 2.10�ȏ���C���X�g�[�����Ă��������D
  Ubuntu�̏ꍇ�Capt��p���Ĉȉ��̂悤�ɃC���X�g�[���ł��܂��D

      apt-get install mono-complete


IKVM.NET (http://www.ikvm.net/)
--------
  ��q���� Apache OpenNLP ��C#�Ŏg�����߂ɕK�v�ȃ��W���[���ł��D
  �����T�C�g���o�C�i�����܂�ZIP�t�@�C�����_�E�����[�h���Ă��������D
  �W�J���č쐬���ꂽ bin �f�B���N�g���ɕK�v�ȃ��W���[��������������܂��D
  �ȉ��̃t�@�C���� {project_root}/lib �f�B���N�g���ɃR�s�[���Ă��������D
    
	IKVM.OpenJDK.Beans.dll		IKVM.OpenJDK.Charsets.dll		IKVM.OpenJDK.Corba.dll
	IKVM.OpenJDK.Core.dll		IKVM.OpenJDK.Jdbc.dll			IKVM.OpenJDK.Management.dll
	IKVM.OpenJDK.Media.dll		IKVM.OpenJDK.Misc.dll			IKVM.OpenJDK.Naming.dll
	IKVM.OpenJDK.Remoting.dll	IKVM.OpenJDK.Security.dll		IKVM.OpenJDK.SwingAWT.dll
	IKVM.OpenJDK.Text.dll		IKVM.OpenJDK.Tools.dll			IKVM.OpenJDK.Util.dll
	IKVM.OpenJDK.XML.API.dll	IKVM.OpenJDK.XML.Bind.dll		IKVM.OpenJDK.XML.Crypto.dll
	IKVM.OpenJDK.XML.Parse.dll	IKVM.OpenJDK.XML.Transform.dll	IKVM.OpenJDK.XML.WebServices.dll
	IKVM.OpenJDK.XML.XPath.dll	IKVM.Reflection.dll				IKVM.Runtime.dll


Apache OpenNLP (http://opennlp.apache.org/)
--------------
  �����T�C�g���C�o�C�i���p�b�P�[�W���_�E�����[�h���Ă��������D
  (���M�����݂��ƁCapache-opennlp-1.5.2-incubating-bin)

  �_�E�����[�h������C���k�t�@�C����W�J���Ă��������D
  ����ƁC���̒��� lib �f�B���N�g�����쐬����܂��D
  �ȉ��C���� lib �f�B���N�g���� {opennlp_dir}/lib �ƕ\�L���܂��D

  Apache OpenNLP��Java�v���O�����ł��邽�߁C��قǓ�������IKVM��p����.NET�A�Z���u���ɕϊ�����K�v������܂��D
  IKVM��bin�f�B���N�g���Ɉړ�����C�������̓p�X��ʂ�����ɁC�ȉ��̃R�}���h�����s���Ă��������D

  * Windows .NET Framework �̏ꍇ
    ikvmc -out:OpenNlp.dll {opennlp_dir}/lib/*.jar

  * Linux Mono �̏ꍇ
    mono ikvmc.exe -out:OpenNlp.dll {opennlp_dir}/lib/*.jar

  �R�}���h���s��C�J�����g�f�B���N�g���� OpenNlp.dll �Ƃ����t�@�C�����쐬����܂��D
  ���̃t�@�C���� {project_root}/lib �f�B���N�g���ɃR�s�[���Ă��������D


SGMLReader (https://github.com/MindTouch/SGMLReader)
----------
  github���C�o�C�i���p�b�P�[�W���_�E�����[�h���Ă��������D
  ���̒��Ɋ܂܂�� SgmlReaderDll.dll �t�@�C���� {project_root}/lib �f�B���N�g���ɃR�s�[���Ă��������D


DotNetZip (http://dotnetzip.codeplex.com/)
---------
  �����T�C�g���C�o�C�i���p�b�P�[�W���_�E�����[�h���Ă��������D
  ���k�t�@�C����W�J������C�ȉ��̃t�@�C���� {project_root}/lib �f�B���N�g���ɃR�s�[���Ă��������D

    {DotNetZip_dir}/Tools/Ionic.Zip.dll


ConsoleLib (https://bitbucket.org/shokai/consolelibnet)
----------
  ��LURL��bitbucket�ɃA�N�Z�X���C�^�u���j���[���� Source ��I�����Ă��������D
  ����ƁC�v���W�F�N�g�̃f�B���N�g���\�����\������܂��̂ŁC�ȉ��̂悤�ɒH��C
  ConsoleLib.dll �t�@�C�����_�E�����[�h���C{project_root}/lib �f�B���N�g���ɃR�s�[���Ă��������D

    ConsoleLib.NET/ConsoleLib/bin/Release/ConsoleLib.dll


Classias (http://www.chokkan.org/software/classias/index.html.ja)
--------
  �{�V�X�e���őO�u�����̌��o���s���ۂɁC�@�B�w�K������Classias��p���܂��D
  �����T�C�g���_�E�����[�h���āC�C���X�g�[�����s���Ă��������D


=========================================
 �f�[�^
=========================================

  �{�V�X�e���̓���ɕK�v�ȃf�[�^�͈ȉ��̒ʂ�ł��D
    
	�EApache OpenNLP ��̓��f��
	�EWordNet �f�[�^�x�[�X�t�@�C��
	�E�O�u�����ރ��f���t�@�C���i�O�u����茟�o���̂݁j

  �ȍ~�C���ꂼ��ɂ��ďڍׂ�������܂��D


Apache OpenNLP ��̓��f�� (http://opennlp.sourceforge.net/models-1.5/)
------------------------
  ��LURL����C�ȉ��̉p��p��̓��f���t�@�C�����_�E�����[�h���C
  �C�ӂ̃f�B���N�g���ɂ܂Ƃ߂Ĕz�u���Ă��������D

    en-token.bin		en-sent.bin				en-pos-maxent.bin
	en-chunker.bin		en-ner-date.bin			en-ner-location.bin
	en-ner-money.bin	en-ner-organization.bin	en-ner-percentage.bin
	en-ner-person.bin	en-ner-time.bin

  �������CPOS�^�O�t�����f���� en-pos-maxent.bin �́C���̂܂܂ł͕s��������Ďg�p�ł��܂���D
  �ȉ��̎菇�ŏC�����s���Ă��������D

    1. en-pos-maxent.bin ��ZIP�t�@�C���Ƃ݂Ȃ��ēW�J����
	2. manifest.properties, tags.tagdict, pos.model ��3�̃t�@�C�����W�J�����
	3. tags.tagdict ������2�t�@�C�����ēxZIP���k����i�܂�܂܂��t�@�C���� manifest.properties, pos.model�j
	4. 3.�ō쐬����ZIP�t�@�C���� en-pos-maxent-fix.bin �Ƃ������O�ŁC���̃��f���t�@�C���Ɠ����f�B���N�g���ɕۑ�����


WordNet �f�[�^�x�[�X�t�@�C�� (http://wordnet.princeton.edu/wordnet/download/current-version/)
----------------------
  ��LURL���CWordNet 3.0 for UNIX-like systems �̒��ɂ���C
  Download just database files: WNdb-3.0.tar.gz
  ���_�E�����[�h���C�W�J���Ă��������D

  �W�J��C�쐬����邷�ׂẴt�@�C�����܂߂�ZIP���k���s���Ă��������D
  �Ō�ɁC����ZIP�t�@�C���� wn.zip �Ƃ������O�ŕۑ����C
  OpenNLP�̉�̓��f���t�@�C���Ɠ����f�B���N�g���ɔz�u���Ă��������D


�O�u�����ރ��f���t�@�C���i�O�u����茟�o���̂݁j
----------------------------------------
  ���Ɋw�K�ς݂̑O�u�����f���t�@�C�����ȉ��̏ꏊ�ɕt�����Ă��܂��D

    {project_root}/prp_models/

  ���f���t�@�C���́C�ΏۑO�u�����Ƃɗp�ӂ���Ă���C�ȉ���9�t�@�C���ł��D

    about.train.model	at.train.model		by.train.model
	for.train.model		from.train.model	in.train.model
	of.train.model		on.train.model		to.train.model

  �t���̑O�u�����f���Ɠ���̂��̂��쐬����ꍇ�C�ȉ��̃��\�[�X���K�v�ƂȂ�܂��D
    
	�E�p���Y Ver. 128 �t���̗ᕶ�W �u�Ꭻ�Y�v
	�EEDR�d�q������ Ver. 2.0 �p��R�[�p�X

  ����烊�\�[�X�C�܂��͑��̃��\�[�X����O�u�����f�����\�z������@�ɂ��ẮC��q�́u�g�����v���������������D


=========================================
 �V�X�e���̃r���h���@
=========================================

  �e���Ŗ{�V�X�e�����r���h������@��������܂��D
  �r���h�̍ۂ́C�O�q�������O�ɕK�v�ȃ��W���[�����w��̏ꏊ�ɏ������Ă����Ă��������D

Windows
-------
  Visual Studio�𗘗p������@�ƁCMSBuild�𗘗p������@��2������܂��D

  Visual Studio�̃o�[�W����2010�ȏ�ł���΁C�\�����[�V�����t�@�C�� OtotakeEdcw2012.sln �𒼐ڊJ�����Ƃ��\�ł��D
  �\�����[�V������Release�r���h���Ă��������D

  .NET Framework�ɕt������MSBuild��p���ăr���h���邱�Ƃ��\�ł��D
  .NET Framework 4.0 ���C���X�g�[���ς݂ł���΁C�ȉ��̏ꏊ��MSBuild�̎��s�t�@�C��������͂��ł��D

    C:\Windows\Microsoft.NET\Framework\v4.0.30319\MSBuild.exe

  ��L��MSBuild.exe��C�ӂ̏ꏊ�Ŏ��s�ł���悤�C�f�B���N�g�������ϐ�PATH�ɒǉ����Ă��������D
  ���̌�C�{�V�X�e�����r���h����ɂ́C�R�}���h�v�����v�g�ňȉ��̎菇�𓥂�ł��������D

    cd {project_root}
	msbuild.exe /p:Configuration=Release

Linux
-----
  Mono�ɕt������MSBuild�݊��v���O������ xbuild ���g�p���ăr���h���s���܂��D
  �{�V�X�e�����r���h����ɂ́C�ȉ��̃R�}���h�����ɓ��͂��Ă��������D

    cd {project_root}
	xbuild /p:Configuration=Release


=========================================
 �g����
=========================================

  �O�q�����菇�Ńr���h����������ƁC�ȉ��̏ꏊ�ɖ{�V�X�e���̎��s�t�@�C�����쐬����܂��D

    {project_root}/Console/bin/Release/Console.exe

  Windows��.NET Framework�̊��̏ꍇ�́C��L���s�t�@�C���͒��ڎ��s�\�ł��D
  Mono�𗘗p����ꍇ�́C���s�̍ۂ͈ȉ��̂悤�ɓ��͂��Ă��������D

    mono Console.exe

  �{�v���O�����͑��p�����[�^�ɂ���āC�����̋@�\��񋟂��܂��D
  �p�����[�^�Ƌ@�\�̊֌W�͈ȉ��̒ʂ�ł��D

    Console.exe d :	KJ-Corpus��ΏۂƂ�����茟�o
	Console.exe t :	�O�u���p�g���[�j���O�f�[�^�쐬
	Console.exe r :	����`���̃f�[�^�̐��e�L�X�g�ϊ�

  �ȍ~�C�e�@�\�̏ڍׂ�������܂��D

KJ-Corpus��ΏۂƂ�����茟�o
----------------------------
  * ���s���@

    Console.exe d [--target TYPE (prp | v_agr)] [--kjdir KJ-CORPUS-DIR]
                  [--formal] [--res RESOURCE-DIR]
                  [--out OUTPUT-FILE] [--model MODELS-DIR]
				  [--classias PATH-TO-CLASSIAS-TAG]

  * �e�I�v�V�����ɂ���
    
	--target TYPE
		��茟�o�Ώۂ��w�肷��Dprp (�O�u��) �������́Cv_agr (����)

	--kjdir KJ-CORPUS-DIR
		��茟�o�Ώۂ�KJ Corpus�� corpus_data �f�B���N�g���p�X���w�肷��D

	--formal
		�t�H�[�}�������p�X�C�b�`�D
		�w�肵��KJ Corpus�� .edc �� .pos �t�@�C���������݂��Ȃ��ꍇ�́C�{�I�v�V�������w�肷��D

	--res RESOURCE-DIR
		OpenNLP�C�����WordNet�̃��\�[�X�t�@�C��������f�B���N�g�����w�肷��D

	--out OUTPUT-FILE
		��茟�o���ʂ̏o�̓t�@�C�������w�肷��D
		���ʂ�ZIP�t�@�C���ƂȂ�D

	--model MODELS-DIR
		(�O�u�����̂Ƃ��̂ݕK�v)
		�O�u�����ރ��f���t�@�C��������f�B���N�g�����w�肷��D

	--classias PATH-TO-CLASSIAS-TAG
		�@�B�w�K�����ł���Classias�̕��ފ�v���O�����ł��� classias-tag �̃t���p�X���w�肵�܂��D
		�ȗ����ꂽ�ꍇ�C/usr/local/bin/classias-tag �Ƃ݂Ȃ��Ď��s���܂��D


�O�u���p�g���[�j���O�f�[�^�쐬
-------------------------
  ���p���e�L�X�g����͂Ƃ��C�e�O�u���ɑΉ�����Classias��l���ޗp�g���[�j���O�f�[�^���o�͂��܂��D
  �o�͂����t�@�C���͈ȉ��̒ʂ�ł��D

    about.train	at.train	by.train
	for.train	from.train	in.train  
	of.train	on.train	to.train

  * ���s���@

    Console.exe t [--out MODELS-OUTPUT-DIR] [--src SOURCE-TEXT-FILE]
                  [--res RESOURCE-DIR]

  * �e�I�v�V�����ɂ���

    --out DATA-OUTPUT-DIR
		�e�O�u���̃g���[�j���O�f�[�^���o�͂���f�B���N�g�����w�肷��D

	--src SOURCE-TEXT-FILE
		�\�[�X�ƂȂ鐶�e�L�X�g�t�@�C�����w�肷��D

	--res RESOURCE-DIR
		OpenNLP�C�����WordNet�̃��\�[�X�t�@�C��������f�B���N�g�����w�肷��D

  * �g���[�j���O�f�[�^����O�u�����f�����쐬������@
    
	�쐬�����g���[�j���O�f�[�^�e�X�ɂ��āCClassias�̊w�K��v���O�����ł��� classias-train ��K�p�����邱�ƂŁC
	�e�O�u���̓�l���ރ��f�����쐬���܂��D
	classias-train �ւ̃p�X���ʂ��Ă��邱�Ƃ��m�F���C�ȉ��̃R�}���h���e�g���[�j���O�f�[�^�ɑ΂��čs���Ă��������D
	({prp} �� �e�O�u���ɒu�������Ă��������D)

	  classias-train -tb -a lbfgs.logistic -m {prp}.train.model {prp}.train

	���ʂƂ��č쐬����郂�f���t�@�C���́C���ׂē���̃f�B���N�g���ɕۑ����Ă����Ă��������D


����`���̃f�[�^�̐��e�L�X�g�ϊ�
---------------------------
  ����`���̃f�[�^���C�g���[�j���O�f�[�^�쐬�p�̐��e�L�X�g�ɕϊ����܂��D

  * ���s���@

    Console.exe r [--type DATA-TYPE (edr | reijiro)] [--file DATA-FILE]

  * �e�I�v�V�����ɂ���
    
	--type DATA-TYPE
		�f�[�^�`�����w�肷��D
		���ݑΉ��\�Ȍ`���͈ȉ��̒ʂ�D
			edr:		EDR�d�q������ Ver. 2.0 �p��R�[�p�X (ECO.DIC)
			reijiro:	�p���Y�t���̗ᕶ�W �u�Ꭻ�Y�v (Ver.128�ł���΁CREIJI128.TXT)
						�i�������CREIJIxxx.TXT��UTF-8�ȊO�̕����R�[�h�ŋL�q����Ă���ꍇ�C
						  UTF-8�ɕϊ����Ă���C�{�v���O������K�p����K�v������D�j

	--file DATA-FILE
		�f�[�^�t�@�C�����w�肷��D