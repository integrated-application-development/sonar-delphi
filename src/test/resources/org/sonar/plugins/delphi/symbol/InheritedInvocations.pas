unit InheritedInvocations;

{This is a sample Delphi file.}

interface

type
  TBaseFoo = class
    procedure Proc;
  end;

  TFoo = class(TBaseFoo)
  public
    function Func(A: Integer; B: String): TFoo;
    function Func: TFoo;
  end;

  TBar = class(TFoo)
  public
    function Func(A: Integer; B: String): TFoo; override; overload;
    function Func: TFoo; override; overload;
    function Func(C: String): TFoo; overload;
  end;

implementation

  function TFoo.Func(A: Integer; B: String): TFoo;
  begin
    // Do nothing
  end;

  function TFoo.Func: TFoo;
  begin
    // Do nothing
  end;

  function TBar.Func(A: Integer; B: String): TFoo;
  begin
    inherited;
    inherited.Proc;
    inherited.Func;
    inherited.Func(1, 'test');
    inherited.Func.SomeNonexistentFunction;
    inherited Func;
    inherited Func(A, B);
    Func;
  end;

  function TBar.Func: TFoo;
  begin
    inherited;
    inherited.Proc;
    inherited.Func;
    inherited.Func(1, 'test');
    inherited.Func.SomeNonexistentFunction;
    inherited Func;
    inherited Func(1, 'test');
    Func(1, 'test');
    Func('test');
  end;

  function TBar.Func(C: String): TFoo;
  begin
    inherited Func.Func(1, 'test');
    inherited Func(1, 'test').Func;
  end;

end.