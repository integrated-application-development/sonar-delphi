unit Properties;

interface

type
  TIntAlias = Integer;

  TMyObj = class(TObject)
  private
    FField: Integer;

    procedure SetMyFieldConst(const Val: Integer);
    procedure SetMyFieldAlias(Val: TIntAlias);
    procedure SetMyField(Val: Integer);

    function GetMyField: Integer;
    function GetMyFieldAlias: TIntAlias;
  public
    property Prop1: Integer read FField write FField;
    property Prop2: Integer read GetMyField write SetMyField;
    property Prop3: Integer read GetMyField write SetMyFieldConst;
    property Prop4: Integer read GetMyFieldAlias write SetMyFieldAlias;
  end;

  TIndexObj = class(TObject)
  private
    FField: TObject;

    function GetMyField(Index: Integer): TObject;
    procedure SetMyField(Index: Integer; Value: TObject);
    function IsStored(Index: Integer): Boolean;
  public
    property Prop1: TObject index 0 read GetMyField write SetMyField stored IsStored;
  end;

implementation

end.