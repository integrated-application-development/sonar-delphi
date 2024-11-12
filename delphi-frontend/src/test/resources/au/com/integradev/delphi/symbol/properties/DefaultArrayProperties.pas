unit DefaultArrayProperties;

interface

implementation

type
  TEnum = (Flarp);

  TFoo = class
    function GetBaz(I: Integer): Integer;
    property Baz[I: Integer]: Integer read GetBaz; default;
  end;

  TBar = class(TFoo)
    function GetBaz(S: string): string; overload;
    function GetBaz(E: TEnum): TEnum; overload;
    property Baz[S: string]: string read GetBaz; default;
    property Baz[E: TEnum]: TEnum read GetBaz; default;
  end;

function TFoo.GetBaz(I: Integer): Integer;
begin
  Result := I;
end;

function TBar.GetBaz(S: string): string;
begin
  Result := S;
end;

function TBar.GetBaz(E: TEnum): TEnum;
begin
  Result := E;
end;

function BarFunc: TBar;
begin
  Result := TBar.Create;
end;

procedure Test(Bar: TBar);
begin
  var A := Bar.Baz[123];
  var B := Bar.Baz['123'];
  var C := Bar.Baz[Flarp];

  var D := BarFunc.Baz[123];
  var E := BarFunc.Baz['123'];
  var F := BarFunc.Baz[Flarp];
end;

end.